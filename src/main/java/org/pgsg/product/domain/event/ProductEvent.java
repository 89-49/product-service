package org.pgsg.product.domain.event;

import java.util.UUID;

public record ProductEvent(
	UUID productId
) {
}
