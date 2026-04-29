package org.pgsg.product.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.pgsg.product.domain.model.Product;

public interface ProductRepository {
	Product save(Product product);

	Optional<Product> findById(UUID id);

	Product saveAndFlush(Product product);
}
