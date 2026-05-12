package org.pgsg.product.global.exception;

import org.pgsg.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode{
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
	ProductNotFoundException("product.resource.not-found.product"),
	Forbidden("product.validation.user.forbidden"),
	Unauthorized("product.validation.user.unauthorized");


	private final String errorKey;

	@Override
	public String getErrorKey() {
		return this.errorKey;
	}
}
