package org.pgsg.product.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest (
	@NotBlank String name,
	@NotNull @Min(value = 0, message = "가격은 0원 이상이어야 합니다.") Integer price,
	String description,
	@NotNull LocalDateTime startTime,
	@NotNull LocalDateTime endTime
){
}
