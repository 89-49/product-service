package org.pgsg.product.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest (
	@NotBlank String name,
	@NotNull @Min(value = 0, message = "가격은 0원 이상이어야 합니다.") Integer price,
	String description
	//todo: 추후 타임딜 초기 설정을 위해 시작시간/종료시간 추가 예정
){
}
