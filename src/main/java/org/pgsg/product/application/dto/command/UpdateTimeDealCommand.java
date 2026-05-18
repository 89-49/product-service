package org.pgsg.product.application.dto.command;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.time.LocalDateTime;
import java.util.Objects;

import org.pgsg.common.exception.CustomException;
import org.pgsg.common.exception.ErrorCode;

public record UpdateTimeDealCommand(
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public UpdateTimeDealCommand {
		Objects.requireNonNull(startTime, "타임딜 시작 시간은 필수입니다.");
		Objects.requireNonNull(endTime, "타임딜 종료 시간은 필수입니다.");

		if (startTime.isBefore(LocalDateTime.now()))
			throw new CustomException(InvalidChangeScheduleException, "startTime");

		if (endTime.isBefore(startTime.plusMinutes(15)))
			throw new CustomException(InvalidTimeDealDurationException, "end");
	}
}
