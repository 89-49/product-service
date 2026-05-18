package org.pgsg.product.domain.model;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

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


	/*
	상품 생성
	*/
	public static Product create(String name, Integer price, String description, LocalDateTime startTime, LocalDateTime endTime) {
		validatePrice(price);

		Objects.requireNonNull(name,"상품명이 누락되었습니다.");

		Product product = new Product();

		product.name = name;
		product.price = price;
		product.description = description;
		product.validateTimeDealSchedule(startTime,endTime);
		product.timeDealSchedule = TimeDealSchedule.of(startTime, endTime);
		product.status=ProductStatus.PENDING_SALE;
		product.reserve();

		return product;
	}

	/*
	타임딜 스케줄링
	*/
	public void updateTimeDeal(LocalDateTime newStart, LocalDateTime newEnd) {
		if(!isUpdatableStatus())
			throw new CustomException(InvalidStatusException,"status");

		validateTimeDealSchedule(newStart, newEnd);

		this.timeDealSchedule = TimeDealSchedule.of(newStart, newEnd);
		if (status == ProductStatus.PENDING_SALE)
			reserve();
	}

	/*
	상태 전이
	*/
	//예약 대기 중 -> 예약 진행 중
	public void startReserve() {
		if(!status.equals(ProductStatus.PENDING_RESERVATION))
			throw new CustomException(InvalidStatusException,"status");
		status = ProductStatus.RESERVING;
	}

	//예약 진행 중 -> 거래 진행 중
	public void startTrade() {
		if(!status.equals(ProductStatus.RESERVING))
			throw new CustomException(InvalidStatusException,"status");
		status = ProductStatus.IN_TRADE;
	}

	//거래 진행 중 -> 거래 완료
	public void complete() {
		if(!status.equals(ProductStatus.IN_TRADE))
			throw new CustomException(InvalidStatusException,"status");
		status = ProductStatus.COMPLETED;
	}

	//거래 또는 예약 취소로 인해 예약 진행 중으로 변경
	public void revertToReserving() {
		if(!status.equals(ProductStatus.RESERVING)&&!status.equals(ProductStatus.IN_TRADE))
			throw new CustomException(InvalidStatusException,"status");
		status = ProductStatus.RESERVING;
	}

	//판매 대기 중 -> 예약 대기 중
	public void reserve() {
		if(!status.equals(ProductStatus.PENDING_SALE))
			throw new CustomException(InvalidStatusException,"status");
		status = ProductStatus.PENDING_RESERVATION;
	}

	//판매 취소
	public void cancelSale() {
		status = ProductStatus.SALE_CANCELED;
	}

	/*
	* 상품 정보 수정
	* */
	public void updateInfo(String newName, Integer newPrice, String newDescription) {
		if(!isUpdatableStatus())
			throw new CustomException(InvalidStatusException,"status");

		if(!(newName ==null || newName.isBlank()))
			name = newName.trim();

		if(newPrice != null) {
			validatePrice(newPrice);
			price = newPrice;
		}

		if(!(newDescription == null || newDescription.isBlank()))
			description = newDescription;
	}



	/*
	상품 삭제 - soft delete + 거래 취소로 상태 변경
	* */
	public void deleteProduct(UUID userId) {
		delete(userId);
		cancelSale();
	}


	//내부 검증
	private static void validatePrice(Integer price) {
		Objects.requireNonNull(price);
		if (price < 0)
			throw new CustomException(PriceValidateException,"price");
	}

	private boolean isUpdatableStatus() {
		return status == ProductStatus.PENDING_RESERVATION ||
			status == ProductStatus.PENDING_SALE;
	}

	private void validateTimeDealSchedule(LocalDateTime start, LocalDateTime end) {	//todo: 정책적 검증의 경우 추가되는 양에 따라 분리 고려
		Objects.requireNonNull(start,"시작 시간이 누락되었습니다.");
		Objects.requireNonNull(end,"종료 시간이 누락되었습니다.");

		if (LocalDateTime.now().isAfter(start)) {
			throw new CustomException(InvalidChangeScheduleException, "startTime");
		}

		if(end.isBefore(start.plusMinutes(15)))
			throw new CustomException(InvalidTimeDealDurationException,"end");
	}
}
