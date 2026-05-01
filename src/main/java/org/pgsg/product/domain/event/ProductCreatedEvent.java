package org.pgsg.product.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductCreatedEvent(
	UUID productId,
	String name,
	Integer price,
	LocalDateTime endTime,
	UUID sellerId
) {
}
