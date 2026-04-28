package org.pgsg.product.application.dto.info;

import java.util.Objects;
import java.util.UUID;

public record ProductInfo(
	UUID productId,
	String name
) {
	public ProductInfo{
		Objects.requireNonNull(productId, "상품ID는 필수입니다.");
		Objects.requireNonNull(name, "상품명은 필수입니다.");
	}
}
