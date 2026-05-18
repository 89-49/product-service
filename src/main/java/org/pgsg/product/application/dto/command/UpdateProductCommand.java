package org.pgsg.product.application.dto.command;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.time.LocalDateTime;

import org.pgsg.common.exception.CustomException;

public record UpdateProductCommand(
	String name,
	Integer price,
	String description
) {
	public UpdateProductCommand {
		if(price!=null&&price<0)
			throw new CustomException(PriceValidateException,"price");
	}
}
