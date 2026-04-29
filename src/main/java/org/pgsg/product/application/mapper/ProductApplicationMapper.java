package org.pgsg.product.application.mapper;

import org.mapstruct.Mapper;
import org.pgsg.product.application.dto.info.ProductInfo;
import org.pgsg.product.application.dto.result.FindProductResult;
import org.pgsg.product.domain.model.Product;

@Mapper(componentModel = "spring")
public interface ProductApplicationMapper {
	//entity -> dto
	ProductInfo toInfoResult(Product product);
	FindProductResult toFindResult(Product product);
}
