package org.pgsg.product.presentation.dto.response;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductResponse(
	@NotNull UUID id,
	@NotBlank String name,
	@NotNull Integer price,
	String description
	//todo: 타임딜 정보의 반환 필요성 고려
) {
}
