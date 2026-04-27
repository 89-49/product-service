package org.pgsg.product.presentation.dto.response;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProductResponse(
	@NotBlank String name,
	@NotNull String price,
	String Description,
	LocalDateTime startTime,	//todo: 타임딜 정보는 방식 수정 후 검증 설정 예정
	LocalDateTime endTime
) {
}
