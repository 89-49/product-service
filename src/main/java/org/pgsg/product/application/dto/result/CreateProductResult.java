package org.pgsg.product.application.dto.result;

import java.util.Objects;

import org.pgsg.common.exception.CustomException;

public record CreateProductResult(
	String name,
	Integer price,
	String description
) {
	public CreateProductResult {
			Objects.requireNonNull(name, "상품명은 필수입니다.");
			Objects.requireNonNull(price,"가격은 필수입니다.");

			if(name.isBlank())
				throw new CustomException("ProductNameValidException","name");
			if(price<0)
				throw new CustomException("PriceValidateException","price");
		}
		}
