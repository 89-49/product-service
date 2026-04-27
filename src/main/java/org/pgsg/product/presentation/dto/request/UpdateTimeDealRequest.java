package org.pgsg.product.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record UpdateTimeDealRequest(
	// LocalDateTime startTime,	//todo: mvp에서는 종료 시간만 설정
	@NotNull LocalDateTime endtime
) {
}
