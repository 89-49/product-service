package org.pgsg.product.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.pgsg.common.response.CommonResponse;
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.service.ProductCommandService;
import org.pgsg.product.presentation.dto.request.CreateProductRequest;
import org.pgsg.product.presentation.dto.request.UpdateProductRequest;
import org.pgsg.product.presentation.dto.request.UpdateTimeDealRequest;
import org.pgsg.product.presentation.dto.response.CreateProductResponse;
import org.pgsg.product.presentation.dto.response.FindProductResponse;
import org.pgsg.product.presentation.dto.response.ProductListItem;
import org.pgsg.product.presentation.dto.response.UpdateProductResponse;
import org.pgsg.product.presentation.mapper.ProductMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductOuterController {
	private final ProductCommandService productCommandService;
	private final ProductMapper mapper;

	//상품 등록
	@PostMapping
	public CommonResponse<CreateProductResponse> addProduct(@Valid @RequestBody CreateProductRequest request) {
		CreateProductCommand command=mapper.toCommand(request);
		CreateProductResponse response=mapper.toResponse(
			productCommandService.createProduct(command));
		return CommonResponse.success(response);
	}

	//상품 삭제
	@DeleteMapping("/{productId}")
	public CommonResponse<Void> deleteProduct(@PathVariable UUID productId) {
		productCommandService.deleteProduct(productId);
		return  CommonResponse.success(null);
	}

	//스케줄 설정 - //todo: mvp 임시 요청
	@PatchMapping("/{productId}/schedule")
	public CommonResponse<UpdateProductResponse> updateTimeDealSchedule(@PathVariable UUID productId,@Valid @RequestBody UpdateTimeDealRequest request) {
		return null;	//todo: 응용 계층 구현 후 수정
	}


	//상품 정보 수정
	@PatchMapping("/{productId}")
	public CommonResponse<UpdateProductResponse> updateProduct(@PathVariable UUID productId, @Valid @RequestBody UpdateProductRequest request) {
		return null;	//todo: 응용 계층 구현 후 수정
	}

	//상품 판매 취소
	@PatchMapping("/{productId}/cancel")
	public void cancelSaleProduct(@PathVariable UUID productId) {
		//todo: 응용 계층 구현 후 수정
	}

	//상품 상세 조회
	@GetMapping("/{productId}")
	public CommonResponse<FindProductResponse> findProductById(@PathVariable UUID productId) {
		return null;	//todo: 응용 계층 구현 후 수정
	}

	//상품 목록 조회
	@GetMapping
	public CommonResponse<Slice<ProductListItem>> getAllProducts(
		@PageableDefault(size = 10, direction = Sort.Direction.DESC) Pageable pageable) {
		return null;	//todo: 응용 계층 구현 후 수정
	}

	//고도화: 상품 검색, 관심 상품 추가, 관심 상품 삭제


	//util
	private Pageable validatePageSize(Pageable pageable) {
		List<Integer> allowed = List.of(10, 30, 50);

		return  allowed.contains(pageable.getPageSize())
			? pageable
			: PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
	}
}
