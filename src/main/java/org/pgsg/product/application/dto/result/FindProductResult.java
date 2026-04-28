package org.pgsg.product.application.dto.result;

import java.time.LocalDateTime;
import java.util.Objects;

public record FindProductResult(
	String name,
	Integer price,
	String description,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public FindProductResult{
		Objects.requireNonNull(name, "상품명은 필수입니다.");
		Objects.requireNonNull(price,"가격은 필수입니다.");
	}
}
