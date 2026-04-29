package org.pgsg.product.application.service;

import static org.pgsg.product.global.exception.ProductException.*;

import java.util.UUID;

import org.pgsg.common.exception.CustomException;
import org.pgsg.product.application.dto.info.ProductInfo;
import org.pgsg.product.application.dto.result.FindProductResult;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.presentation.mapper.ProductMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ProductQueryService {
	private final ProductRepository productRepository;	//todo: cqrs 적용 시 변경
	private final ProductMapper productMapper;

	public FindProductResult findProduct(UUID id) {
		return productRepository.findById(id)
			.map(productMapper::toFindResult)
			.orElseThrow(()->new CustomException(ProductNotFoundException));
	}

	public Slice<ProductInfo> getProducts(Pageable pageable) {
		return productRepository.findAll(pageable)
			.map(productMapper::toInfoResult);
	}


	//todo: 고도화: 관리자와 사용자의 상품 조회 분리, cqrs 적용, 상품 검색,
}
