package org.pgsg.product.infrastructure.repository;

import java.util.UUID;

import org.pgsg.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product,UUID>{
}
