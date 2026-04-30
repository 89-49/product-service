package org.pgsg.product.application.dto.result;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.time.LocalDateTime;

import org.pgsg.common.exception.CustomException;

public record UpdateProductResult(String name,
                                  Integer price,
                                  String description,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime
) {
	public UpdateProductResult {
		if(price!=null&&price<0)
			throw new CustomException(PriceValidateException,"price");

		if(startTime!=null && endTime!=null
			&&endTime.isBefore(startTime.plusMinutes(15)))
			throw new CustomException(InvalidTimeDealDurationException,"end");
	}
}
