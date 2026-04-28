package org.pgsg.product.application.dto.command;

import static org.pgsg.product.global.exception.ProductException.*;

import java.util.Objects;

import org.pgsg.common.exception.CustomException;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductCommand(
	String name,
	Integer price,
	String description
) {
	public CreateProductCommand {
		Objects.requireNonNull(name, "상품명은 필수입니다.");
		Objects.requireNonNull(price,"가격은 필수입니다.");

		if(name.isBlank())
			throw new CustomException(ProductNameValidException,"name");
		if(price<0)
			throw new CustomException(PriceValidateException,"price");
	}
}
