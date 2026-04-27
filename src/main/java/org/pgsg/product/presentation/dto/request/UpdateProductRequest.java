package org.pgsg.product.presentation.dto.request;

import java.time.LocalDateTime;

public record UpdateProductRequest(
	String name,
	String price,
	String Description,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
