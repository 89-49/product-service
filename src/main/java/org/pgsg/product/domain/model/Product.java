package org.pgsg.product.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.pgsg.common.domain.BaseEntity;
import org.pgsg.common.exception.CustomException;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer price;

	@Column
	private String description;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	@Embedded
	private TimeDealSchedule timeDealSchedule;


	//상품 생성
	public static Product create(String name, Integer price, String description/*, TimeDealSchedule timeDealSchedule*/) {
		validatePrice(price);

		Objects.requireNonNull(name,"상품명이 누락되었습니다.");

		Product product = new Product();

		product.name = name;
		product.price = price;
		product.description = description;
		//todo: mvp에선 타임딜 설정을 분리하여 구현하고, 추후 고도화 시 스케줄링 솔루션 적용 시엔 두 방식 다 사용 가능하도록 수정 예정
		//product.timeDealSchedule = TimeDealSchedule.of(timeDealSchedule.getStartTime(), timeDealSchedule.getEndTime());
		product.status=ProductStatus.PENDING_SALE;	//todo: mvp를 위해 판매 대기 중으로 설정, 추후 생성 시 분기 등에 의해 결정되도록 수정 예정

		return product;
	}

	private static void validatePrice(Integer price) {
		Objects.requireNonNull(price);
		if (price < 0)
			throw new CustomException("PriceValidateException","price");
	}

	//요청 시간 기준으로 타임딜 시간 설정
	public void setTimeDealSchedule(LocalDateTime end) {
		LocalDateTime now = LocalDateTime.now();
		validateTimeDealSchedule(now,end);

		this.timeDealSchedule=TimeDealSchedule.of(now,end);
		this.status=ProductStatus.PENDING_RESERVATION;
	}

	//상태 변경	//todo: 다른 도메인과의 협업 시 status 변경 로직 분리 고려
	//예약 대기 중 -> 예약 진행 중
	public void startReserve() {
		if(!status.equals(ProductStatus.PENDING_RESERVATION))
			throw new CustomException("InvalidStatusException","status");
		status = ProductStatus.RESERVING;
	}

	//예약 진행 중 -> 거래 진행 중
	public void startTrade() {
		if(!status.equals(ProductStatus.RESERVING))
			throw new CustomException("InvalidStatusException","status");
		status = ProductStatus.IN_TRADE;
	}

	//거래 진행 중 -> 거래 완료
	public void complete() {
		if(!status.equals(ProductStatus.IN_TRADE))
			throw new CustomException("InvalidStatusException","status");
		status = ProductStatus.COMPLETED;
	}

	//거래 또는 예약 취소로 인해 예약 진행 중으로 변경
	public void revertToReserving() {
		if(!status.equals(ProductStatus.RESERVING)&&!status.equals(ProductStatus.IN_TRADE))
			throw new CustomException("InvalidStatusException","status");
		status = ProductStatus.RESERVING;
	}

	//판매 대기 중 -> 예약 대기 중
	public void reserve() {
		if(!status.equals(ProductStatus.PENDING_SALE))
			throw new CustomException("InvalidStatusException","status");
		status = ProductStatus.PENDING_RESERVATION;
	}

	//판매 취소
	public void cancelSale() {
		status = ProductStatus.SALE_CANCELED;
	}

	//상품 정보 변경
	public void update(String newName, Integer newPrice, String newDescription, TimeDealSchedule newTimeDealSchedule) {
		if(!isUpdatableStatus())
			throw new CustomException("InvalidStatusException","status");

		if(!(newName ==null || newName.isBlank()))
			name = newName.trim();

		if(newPrice != null) {
			validatePrice(newPrice);
			price = newPrice;
		}

		if(!(newDescription == null || newDescription.isBlank()))
			description = newDescription;

		if(newTimeDealSchedule != null)
			changeTimeDealSchedule(newTimeDealSchedule.getStartTime(), newTimeDealSchedule.getEndTime());
	}

	//타임딜 스케줄 변경
	public void changeTimeDealSchedule(LocalDateTime newStart, LocalDateTime newEnd) {
		if(!isUpdatableStatus())
			throw new CustomException("InvalidStatusException","status");

		validateTimeDealSchedule(newStart, newEnd);

		this.timeDealSchedule = TimeDealSchedule.of(newStart, newEnd);
	}

	private void validateTimeDealSchedule(LocalDateTime start, LocalDateTime end) {	//todo: 정책적 검증의 경우 추가되는 양에 따라 분리 고려
		Objects.requireNonNull(start,"시작 시간이 누락되었습니다.");
		Objects.requireNonNull(end,"종료 시간이 누락되었습니다.");

		// if (LocalDateTime.now().isAfter(start)) {	//todo: mvp 이후 고도화 시 해당 부분 적용 고려
		// 	throw new CustomException("InvalidChangeScheduleException", "startTime");
		// }

		if(end.isBefore(start.plusMinutes(15)))
			throw new CustomException("InvalidTimeDealDurationException","end");
	}

	private boolean isUpdatableStatus() {
		return status == ProductStatus.PENDING_RESERVATION ||
			status == ProductStatus.PENDING_SALE;
	}

	//상품 삭제 - soft delete + 거래 취소로 상태 변경
	public void deleteProduct(UUID userId) {
		delete(userId);
		cancelSale();
	}
}
