package org.pgsg.product.presentation.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FindProductResponse(	//todo: 추후 정보 추가 여부 고려
	@NotBlank String name,
	@NotNull Integer price,
	String description,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
