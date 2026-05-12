package org.pgsg.product.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.pgsg.product.domain.model.ProductStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FindProductResponse(	//todo: 추후 정보 추가 여부 고려
	@NotBlank String name,
	@NotNull Integer price,
	String description,
	UUID sellerId,
	ProductStatus status,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
