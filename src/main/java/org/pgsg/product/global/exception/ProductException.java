package org.pgsg.product.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pgsg.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ProductException implements ErrorCode{
	//도메인 관련
	PriceValidateException("product.validation.domain.price"),
	InvalidStatusException("product.validation.business.update-status"),
	ProductNameValidException("product.validation.domain.name"),

	//타임딜 관련
	EndTimeValidateException("product.validation.domain.timedeal.end"),
	StartTimeValidateException("product.validation.domain.timedeal.start"),
	InvalidChangeScheduleException("product.validation.business.update-timedeal"),
	InvalidTimeDealDurationException("product.validation.business.set-timedeal"),

	//기타
	ProductNotFoundException("product.resource.not-found.product");


	private final String errorKey;

	@Override
	public String getErrorKey() {
		return this.errorKey;
	}
}
