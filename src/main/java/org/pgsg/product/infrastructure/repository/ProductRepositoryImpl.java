package org.pgsg.product.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.repository.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
	private final ProductJpaRepository productJpaRepository;

	@Override
	public Product save(Product product) {
		return productJpaRepository.save(product);
	}

	@Override
	public Optional<Product> findById(UUID id) {
		return productJpaRepository.findById(id);
	}

	@Override
	public Product saveAndFlush(Product product) {
		return productJpaRepository.saveAndFlush(product);
	}

	@Override
	public Slice<Product> findAll(Pageable pageable) {
		return productJpaRepository.findAll(pageable);
	}
}
