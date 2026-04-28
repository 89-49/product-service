package org.pgsg.product.presentation.dto.response;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductListItem(	//todo: 추후 정보 추가 여부 고려
	@NotNull @Valid UUID productId,
	@NotBlank String name
) {
}
