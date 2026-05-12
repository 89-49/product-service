package org.pgsg.product.application.dto.result;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.util.Objects;
import java.util.UUID;

import org.pgsg.common.exception.CustomException;

public record CreateProductResult(
	UUID id,
	String name,
	Integer price,
	String description
) {
	public CreateProductResult {
			Objects.requireNonNull(id,"상품 id가 누락되었습니다.");
			Objects.requireNonNull(name, "상품명은 누락되었습니다.");
			Objects.requireNonNull(price,"가격은 누락되었습니다.");

			if(name.isBlank())
				throw new CustomException(ProductNameValidException,"name");
			if(price<0)
				throw new CustomException(PriceValidateException,"price");
		}
		}
