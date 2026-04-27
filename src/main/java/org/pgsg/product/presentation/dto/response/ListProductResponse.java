package org.pgsg.product.presentation.dto.response;

import org.springframework.data.domain.Slice;

public record ListProductResponse(
	Slice<ProductListItem>	productList
) {
}
