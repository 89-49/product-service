package org.pgsg.product.application.service;

import static org.pgsg.product.global.exception.ProductException.*;

import java.util.UUID;

import org.pgsg.common.exception.CustomException;
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.dto.result.CreateProductResult;
import org.pgsg.product.global.config.security.UserContext;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/*
* 상품 추가, 삭제, 수정 조회, 이벤트 연결
* */
@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ProductCommandService {
	private final ProductRepository productRepository;

	@Transactional
	public CreateProductResult createProduct(CreateProductCommand command) {
		Product product =Product.create(
			command.name(),command.price(),command.description());	//todo: 스케줄 입력 시기 변경 후 수정 필요

		Product saved=productRepository.save(product);

		return new CreateProductResult(saved.getName(),saved.getPrice(),saved.getDescription());
	}

	@Transactional
	public void deleteProduct(UUID id) {
		Product product=productRepository.findById(id)
			.orElseThrow(()->new CustomException(ProductNotFoundException));

		product.deleteProduct(UserContext.getUserId());
	}
}
