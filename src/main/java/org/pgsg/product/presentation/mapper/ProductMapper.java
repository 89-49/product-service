package org.pgsg.product.presentation.mapper;

import org.mapstruct.Mapper;
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.dto.command.UpdateProductCommand;
import org.pgsg.product.application.dto.command.UpdateTimeDealCommand;
import org.pgsg.product.application.dto.info.ProductInfo;
import org.pgsg.product.application.dto.result.CreateProductResult;
import org.pgsg.product.application.dto.result.FindProductResult;
import org.pgsg.product.application.dto.result.UpdateProductResult;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.presentation.dto.request.CreateProductRequest;
import org.pgsg.product.presentation.dto.request.UpdateProductRequest;
import org.pgsg.product.presentation.dto.request.UpdateTimeDealRequest;
import org.pgsg.product.presentation.dto.response.CreateProductResponse;
import org.pgsg.product.presentation.dto.response.FindProductResponse;
import org.pgsg.product.presentation.dto.response.ProductListItem;
import org.pgsg.product.presentation.dto.response.UpdateProductResponse;

@Mapper(componentModel="spring")
public interface ProductMapper {
	//presentation -> application
	CreateProductCommand toCommand(CreateProductRequest request);
	UpdateProductCommand toCommand(UpdateProductRequest request);
	UpdateTimeDealCommand toCommand(UpdateTimeDealRequest request);

	//application -> presentation
	CreateProductResponse toResponse(CreateProductResult result);
	UpdateProductResponse toResponse(UpdateProductResult result);
	FindProductResponse toResponse(FindProductResult result);
	ProductListItem toResponse(ProductInfo productInfo);

	//entity -> dto
	ProductInfo toInfoResult(Product product);
	FindProductResult toFindResult(Product product);
}
