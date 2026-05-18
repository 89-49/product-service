package org.pgsg.product.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record UpdateTimeDealRequest(
	@NotNull LocalDateTime startTime,
	@NotNull LocalDateTime endTime
) {
}
