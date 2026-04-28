package org.pgsg.product.application.dto.result;

import org.pgsg.product.application.dto.info.ProductInfo;
import org.springframework.data.domain.Slice;

public record ListProductResult(
	Slice<ProductInfo> productList
) {
}
