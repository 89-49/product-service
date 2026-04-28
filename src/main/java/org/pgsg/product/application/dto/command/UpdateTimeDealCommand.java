package org.pgsg.product.application.dto.command;

import java.time.LocalDateTime;
import java.util.Objects;

public record UpdateTimeDealCommand(
	// LocalDateTime startTime,
	LocalDateTime endTime
) {
	public UpdateTimeDealCommand {
		Objects.requireNonNull(endTime);
		//	todo: 고도화 후 작업 예정
		// if(endTime.isBefore(start.plusMinutes(15)))
		// 	throw new CustomException(InvalidTimeDealDurationException,"end");
	}
}
