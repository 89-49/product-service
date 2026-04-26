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
	public static Product create(String name, Integer price, String description, TimeDealSchedule timeDealSchedule) {
		validatePrice(price);

		Objects.requireNonNull(name,"상품명이 누락되었습니다.");

		Product product = new Product();

		product.name = name;
		product.price = price;
		product.description = description;
		product.timeDealSchedule = TimeDealSchedule.of(timeDealSchedule.getStartTime(), timeDealSchedule.getEndTime());
		product.status=ProductStatus.PENDING_RESERVATION;

		return product;
	}

	private static void validatePrice(Integer price) {
		Objects.requireNonNull(price);
		if (price < 0)
			throw new CustomException("PriceValidateException","price");
	}

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
			changeTimeDealSchedule(newTimeDealSchedule);
	}

	//타임딜 스케줄 변경
	public void changeTimeDealSchedule(TimeDealSchedule newSchedule) {
		if(!isUpdatableStatus())
			throw new CustomException("InvalidStatusException","status");

		if (LocalDateTime.now().isAfter(this.timeDealSchedule.getStartTime().minusHours(1))) {
			throw new CustomException("InvalidChangeScheduleException", "startTime");
		}

		this.timeDealSchedule = newSchedule;
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
