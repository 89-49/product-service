package org.pgsg.product.domain.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pgsg.common.exception.CustomException;

@DisplayName("Product 도메인 검증")
class ProductTest {
	private static final String VALID_PRODUCT_NAME = "Test Product";
	private static final String VALID_PRODUCT_DESCRIPTION = "Test Product";
	private static final Integer VALID_PRODUCT_PRICE = 100;

	Product validProduct;

	@BeforeEach
	void setUp() {
		TimeDealSchedule schedule =TimeDealSchedule.of(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(3));
		validProduct = Product.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION, schedule);
	}

	@Test
	void create_성공() {
		TimeDealSchedule schedule =TimeDealSchedule.of(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(3));
		Product p = Product.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION, schedule);

		assertThat(p.getName()).isEqualTo(VALID_PRODUCT_NAME);
		assertThat(p.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(p.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
	}

	@Test
	void create_잘못된_가격() {
		TimeDealSchedule schedule =TimeDealSchedule.of(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(3));
		assertThatThrownBy(()->Product
			.create(VALID_PRODUCT_NAME, -1,VALID_PRODUCT_DESCRIPTION, schedule))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_잘못된_종료시간_설정(){
		assertThatThrownBy(()->Product
			.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION,
				TimeDealSchedule.of(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(1))))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_잘못된_시작시간_설정(){
			assertThatThrownBy(()->Product
			.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION,
				TimeDealSchedule.of(LocalDateTime.now().minusHours(1),LocalDateTime.now().plusHours(1))))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void 상태_변경_성공() {
		validProduct.startReserve();
		validProduct.startTrade();
		validProduct.revertToReserving();
		assertThat(validProduct.getStatus()).isEqualTo(ProductStatus.RESERVING);
	}

	@Test
	void 상태_변경_실패() {
		assertThatThrownBy(()->validProduct.complete())
			.isInstanceOf(CustomException.class);
	}


	@Test
	void cancelSale() {
		validProduct.cancelSale();
		assertThat(validProduct.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
	}

	@Test
	void update() {
		validProduct.update("new",null,null,null);
		assertThat(validProduct.getName()).isEqualTo("new");
		assertThat(validProduct.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(validProduct.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
	}

	@Test
	void changeTimeDealSchedule() {
		TimeDealSchedule newSchedule=TimeDealSchedule.of(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(3));
		validProduct.update(null,null,null,newSchedule);
		assertThat(validProduct.getName()).isEqualTo(VALID_PRODUCT_NAME);
		assertThat(validProduct.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(validProduct.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
		assertThat(validProduct.getTimeDealSchedule()).isEqualTo(newSchedule);
	}

	@Test
	void deleteProduct() {
		UUID id=UUID.randomUUID();
		validProduct.deleteProduct(id);
		assertThat(validProduct.getDeletedBy()).isEqualTo(id);
		assertNotNull(validProduct.getDeletedAt());
		assertThat(validProduct.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
	}
}