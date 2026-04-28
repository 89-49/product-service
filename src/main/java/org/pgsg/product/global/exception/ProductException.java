package org.pgsg.product.global.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductException {
	//도메인 관련
	public static final String PriceValidateException="PriceValidateException";
	public static final String InvalidStatusException="InvalidStatusException";
	public static final String ProductNameValidException="ProductNameValidException";

	//타임딜 관련
	public static final String EndTimeValidateException="EndTimeValidateException";
	public static final String StartTimeValidateException="StartTimeValidateException";
	public static final String InvalidChangeScheduleException="InvalidChangeScheduleException";
	public static final String InvalidTimeDealDurationException="InvalidTimeDealDurationException";

	//기타
	public static final String ProductNotFoundException="ProductNotFoundException";

}
