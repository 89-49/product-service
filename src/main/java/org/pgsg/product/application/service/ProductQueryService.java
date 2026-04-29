package org.pgsg.product.application.service;

import java.util.UUID;

import org.pgsg.product.application.dto.result.FindProductResult;
import org.pgsg.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ProductQueryService {
	private final ProductRepository productRepository;	//todo: cqrs 적용 시 변경

	public FindProductResult findProduct(UUID id) {
		return null;
	}


	//todo: 고도화: 관리자와 사용자의 상품 조회 분리, cqrs 적용, 상품 검색,
}
