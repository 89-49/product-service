package org.pgsg.product.application.dto.command;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.time.LocalDateTime;
import java.util.Objects;

import org.pgsg.common.exception.CustomException;

public record CreateProductCommand(
	String name,
	Integer price,
	String description,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public CreateProductCommand {
		Objects.requireNonNull(name, "상품명은 필수입니다.");
		Objects.requireNonNull(price,"가격은 필수입니다.");
		Objects.requireNonNull(startTime, "타임딜 시작 시간은 필수입니다.");
		Objects.requireNonNull(endTime, "타임딜 종료 시간은 필수입니다.");

		if(name.isBlank())
			throw new CustomException(ProductNameValidException,"name");
		if(price<0)
			throw new CustomException(PriceValidateException,"price");
	}
}
