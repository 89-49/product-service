package org.pgsg.product.domain.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.pgsg.product.domain.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductRepository {
	Product save(Product product);

	Optional<Product> findById(UUID id);

	Product saveAndFlush(Product product);

	Slice<Product> findAll(Pageable pageable);
}
