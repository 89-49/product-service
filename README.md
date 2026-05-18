# Product Service

P2P 중고 거래 플랫폼 **PGSG**의 상품 서비스입니다.  
상품의 등록, 수정, 조회 및 타임딜 기반 상태 전이를 담당합니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Database | PostgreSQL 16 |
| Cache | Redis |
| Message Broker | Apache Kafka |
| Build | Gradle |
| Container | Docker |

---

## 도메인 설계

### 상품 상태 (ProductStatus)

| 상태 | 설명 |
|---|---|
| `PENDING_SALE` | 판매 대기 중. 최초 등록 상태 또는 재등록 대기 상태 |
| `PENDING_RESERVATION` | 예약 대기 중. 타임딜이 시작되어 예약 접수 중인 상태 |
| `RESERVED` | 예약 확정. 특정 구매자의 예약이 확정된 상태 |
| `IN_TRADE` | 거래 진행 중. 결제 및 거래가 진행되는 상태 |
| `COMPLETED` | 판매 완료. 거래가 완료된 최종 상태 |
| `SALE_CANCELLED` | 판매 취소. 거래가 취소된 최종 상태 |

### 상태 전이 흐름

**정상 흐름**

```
PENDING_SALE
    └─ reserve() ──────────────────▶ PENDING_RESERVATION
                                            │
                                    confirm() ▼
                                         RESERVED
                                            │
                                    startTrade() ▼
                                          IN_TRADE
                                            │
                                     complete() ▼
                                         COMPLETED
```

**예외 / 역방향 흐름**

| 전이 | 메서드 | 트리거 |
|---|---|---|
| `IN_TRADE` → `RESERVED` | `revertToReserved()` | 구매자 거래 취소 후 다음 예약자 대기 |
| 전 상태 → `PENDING_SALE` | `pendSale()` | 결제 전 판매자 취소 / 타임딜 만료 / 다음 예약자 없을 때 |
| 전 상태 → `SALE_CANCELLED` | `cancelSale()` | 결제 후 취소 이벤트 / 판매자 직접 취소 (`PENDING_SALE`~`PENDING_RESERVATION`) |

**전이 규칙**

- `COMPLETED`, `SALE_CANCELLED` 상태에서는 다른 상태로 전이 불가 (최종 상태)
- `PENDING_SALE` → `IN_TRADE` 직접 전이 불가 (반드시 `PENDING_RESERVATION` → `RESERVED`를 거쳐야 함)
- 상품 등록자(`createdBy`)는 본인 상품을 예약할 수 없음

### Kafka 이벤트 연동

상품 상태 전이는 예약 서비스 및 거래 서비스에서 발행하는 Kafka 이벤트를 구독하여 처리합니다.

| 구독 이벤트 | 처리 메서드 | 전이 |
|---|---|---|
| 예약 서비스 - 타임딜 시작 | `reserve()` | `PENDING_SALE` → `PENDING_RESERVATION` |
| 예약 서비스 - 예약 확정 | `confirm()` | `PENDING_RESERVATION` → `RESERVED` |
| 거래 서비스 - 거래 시작 | `startTrade()` | `RESERVED` → `IN_TRADE` |
| 거래 서비스 - 거래 완료 | `complete()` | `IN_TRADE` → `COMPLETED` |
| 거래 서비스 - 구매자 취소 | `revertToReserved()` | `IN_TRADE` → `RESERVED` |
| 예약 서비스 - 예약 취소 | `pendSale()` | → `PENDING_SALE` |

---

## CQRS 설계

읽기 성능 최적화를 위해 쓰기 모델과 읽기 모델을 분리합니다.

- **쓰기 테이블 (`p_products`)**: 도메인 상태 관리 및 상태 전이 처리
- **읽기 테이블 (`p_products_read`)**: 조회 최적화. 상품 기본 정보 + 판매자 닉네임 등 비정규화 데이터 포함
- 쓰기 이벤트 발행 → Kafka 구독 → 읽기 모델 동기화

---

## API 목록

### 상품 관리

| Method | URI | 설명 | 권한 |
|---|---|---|---|
| `POST` | `/api/v1/products` | 상품 등록 | USER |
| `PATCH` | `/api/v1/products/{productId}` | 상품 수정 (`PENDING_SALE`~`PENDING_RESERVATION` 상태에서만 가능) | USER (본인), MANAGER |
| `DELETE` | `/api/v1/products/{productId}` | 상품 삭제 | USER (본인), MANAGER |
| `PATCH` | `/api/v1/products/{productId}/cancel` | 판매 취소 | USER (본인), MANAGER |

### 상품 조회

| Method | URI | 설명 | 권한 |
|---|---|---|---|
| `GET` | `/api/v1/products` | 상품 목록 조회 (페이징) | ALL |
| `GET` | `/api/v1/products/{productId}` | 상품 단건 조회 | ALL |
| `GET` | `/api/v1/products/my` | 내 상품 목록 조회 | USER |

---

## 환경 변수

| 변수명 | 설명 | 기본값 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | 활성화 프로파일 | `dev,topics,kafka` |
| `CONFIG_SERVER` | Config 서버 주소 | - |
| `PRODUCT_DB_HOST` | DB 호스트 | - |
| `PRODUCT_DB_PORT` | DB 포트 | `5432` |
| `PRODUCT_DB_NAME` | DB 이름 | `p_product` |
| `PRODUCT_DB_USERNAME` | DB 사용자 | - |
| `PRODUCT_DB_PASSWORD` | DB 비밀번호 | - |
| `BOOTSTRAP_SERVERS` | Kafka 브로커 주소 | - |
| `TRUST_STORE_PASSWORD` | Kafka SSL truststore 비밀번호 | - |

---

## 실행 방법

### 로컬 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Docker 실행

```bash
# 환경변수 파일 설정
cp .env.template .env
# .env 파일 내 값 입력

# 실행
docker compose up -d
```

### Docker Compose 구성

```
product-service   : 8082 포트
product-db        : PostgreSQL 16 (5433:5432)
```

---

## 관련 레포지토리

| 레포지토리 | 설명 |
|---|---|
| [common](https://github.com/89-49/common) | 공통 모듈 (보안, 메시징, 예외 처리 등) |
| [infra](https://github.com/89-49/infra) | 인프라 (DB, 모니터링) |
