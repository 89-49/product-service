package org.pgsg.product.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

public record UpdateTimeDealRequest(
	@NonNull LocalDateTime startTime,
	@NotNull LocalDateTime endTime
) {
}
