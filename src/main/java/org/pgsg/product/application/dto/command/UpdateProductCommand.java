package org.pgsg.product.application.dto.command;

import java.time.LocalDateTime;
import java.util.Objects;

import org.pgsg.common.exception.CustomException;

public record UpdateProductCommand(
	String name,
	Integer price,
	String description,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public UpdateProductCommand {
		if(price!=null&&price<0)
			throw new CustomException("PriceValidateException","price");

		if(startTime!=null && endTime!=null
			&&endTime.isBefore(startTime.plusMinutes(15)))
			throw new CustomException("InvalidTimeDealDurationException","end");
	}
}
