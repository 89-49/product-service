package org.pgsg.product.domain.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.util.SecurityUtil;
import org.pgsg.config.security.UserDetailsImpl;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Product 도메인 검증")
class ProductTest {
	private static final String VALID_PRODUCT_NAME = "Test Product";
	private static final String VALID_PRODUCT_DESCRIPTION = "Test Product";
	private static final Integer VALID_PRODUCT_PRICE = 100;
	private MockedStatic<SecurityUtil> securityUtilMockedStatic;

	private Product validProduct;

	@BeforeEach
	void setUp() {
		validProduct = Product.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION);
		validProduct.UpdateTimeDealSchedule(LocalDateTime.now().plusHours(3));
		ReflectionTestUtils.setField(validProduct, "status", ProductStatus.PENDING_RESERVATION);

		securityUtilMockedStatic = mockStatic(SecurityUtil.class);

		securityUtilMockedStatic.when(SecurityUtil::getCurrentUser)
			.thenReturn(Optional.of(UserDetailsImpl.builder()
				.uuid(UUID.randomUUID())
				.username("testuser")
				.password("")
				.userRole("ROLE_MANAGER")
				.name("test")
				.nickname("test")
				.enabled(true)
				.build()));
	}

	@AfterEach
	void tearDown() {
		if (securityUtilMockedStatic != null) {
			securityUtilMockedStatic.close();
			securityUtilMockedStatic = null;
		}
	}

	@Test
	void create_성공() {
		Product p = Product.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION);

		assertThat(p.getName()).isEqualTo(VALID_PRODUCT_NAME);
		assertThat(p.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(p.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
	}

	@Test
	void create_잘못된_가격() {
		assertThatThrownBy(()->Product
			.create(VALID_PRODUCT_NAME, -1,VALID_PRODUCT_DESCRIPTION))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void create_잘못된_종료시간_설정(){
		Product p=Product
			.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION);
		assertThatThrownBy(()->p.UpdateTimeDealSchedule(LocalDateTime.now()))
			.isInstanceOf(CustomException.class);
	}

	// @Test	//todo: 해당 부분은 스케줄링 고도화 시 다시 작성
	// void create_잘못된_시작시간_설정(){
	// 		assertThatThrownBy(()->Product
	// 		.create(VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE,VALID_PRODUCT_DESCRIPTION,
	// 			TimeDealSchedule.of(LocalDateTime.now().minusHours(1),LocalDateTime.now().plusHours(1))))
	// 		.isInstanceOf(CustomException.class);
	// }

	@Test
	void 타임딜_스케줄링_성공(){
		assertThat(validProduct.getTimeDealSchedule()).isNotNull();
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
	void updateInfo() {
		validProduct.updateInfo("new",null,null,null);
		assertThat(validProduct.getName()).isEqualTo("new");
		assertThat(validProduct.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(validProduct.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
	}

	@Test
	void changeTimeDealSchedule() {
		TimeDealSchedule newSchedule=TimeDealSchedule.of(LocalDateTime.now().plusHours(2),LocalDateTime.now().plusHours(3));
		validProduct.updateInfo(null,null,null,newSchedule);
		assertThat(validProduct.getName()).isEqualTo(VALID_PRODUCT_NAME);
		assertThat(validProduct.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(validProduct.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
		assertThat(validProduct.getTimeDealSchedule().getStartTime()).isEqualTo(newSchedule.getStartTime());
		assertThat(validProduct.getTimeDealSchedule().getEndTime()).isEqualTo(newSchedule.getEndTime());
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