package org.pgsg.product.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;

public record UpdateProductRequest(
	String name,
	@Min(value = 0, message = "가격은 0원 이상이어야 합니다.") Integer price,
	String description
) {
}
