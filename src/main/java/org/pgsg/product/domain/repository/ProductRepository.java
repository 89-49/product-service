package org.pgsg.product.domain.repository;

import org.pgsg.product.domain.model.Product;

public interface ProductRepository {
	Product save(Product product);
}
