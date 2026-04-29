package org.pgsg.product.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pgsg.product.application.dto.info.ProductInfo;
import org.pgsg.product.application.dto.result.FindProductResult;
import org.pgsg.product.domain.model.Product;

@Mapper(componentModel = "spring")
public interface ProductApplicationMapper {
	//entity -> dto
	@Mapping(source = "id", target = "productId")
	ProductInfo toInfoResult(Product product);
	FindProductResult toFindResult(Product product);
}
