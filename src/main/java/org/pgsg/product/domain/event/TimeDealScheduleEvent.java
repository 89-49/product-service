package org.pgsg.product.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimeDealScheduleEvent(
	UUID productId,
	LocalDateTime startTime
) {}