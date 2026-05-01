package org.pgsg.product.application.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductEventType {
	CREATED("PRODUCT_CREATED"),
	UPDATED("PRODUCT_UPDATED"),
	DELETED("PRODUCT_DELETED"),
	CANCELLED("PRODUCT_CANCELLED");
	private final String type;

	public String getType() {
		return this.type;
	}
}
