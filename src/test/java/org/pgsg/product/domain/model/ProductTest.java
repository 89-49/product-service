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
	private static final LocalDateTime VALID_START_TIME = LocalDateTime.now().plusHours(1);
	private static final LocalDateTime VALID_END_TIME = LocalDateTime.now().plusHours(3);

	private MockedStatic<SecurityUtil> securityUtilMockedStatic;
	private Product validProduct;

	@BeforeEach
	void setUp() {
		validProduct = Product.create(
			VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_PRODUCT_DESCRIPTION,
			VALID_START_TIME, VALID_END_TIME
		);
		// create() 후 PENDING_RESERVATION 상태이므로 startReserve() 테스트를 위해 그대로 사용
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
		Product p = Product.create(
			VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_PRODUCT_DESCRIPTION,
			VALID_START_TIME, VALID_END_TIME
		);

		assertThat(p.getName()).isEqualTo(VALID_PRODUCT_NAME);
		assertThat(p.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(p.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
		assertThat(p.getTimeDealSchedule()).isNotNull();
		assertThat(p.getStatus()).isEqualTo(ProductStatus.PENDING_RESERVATION);
	}

	@Test
	void create_잘못된_가격() {
		assertThatThrownBy(() -> Product.create(
			VALID_PRODUCT_NAME, -1, VALID_PRODUCT_DESCRIPTION,
			VALID_START_TIME, VALID_END_TIME)
		).isInstanceOf(CustomException.class);
	}

	@Test
	void create_잘못된_시작시간_설정() {
		assertThatThrownBy(() -> Product.create(
			VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_PRODUCT_DESCRIPTION,
			LocalDateTime.now().minusHours(1), VALID_END_TIME)
		).isInstanceOf(CustomException.class);
	}

	@Test
	void create_잘못된_종료시간_설정() {
		assertThatThrownBy(() -> Product.create(
			VALID_PRODUCT_NAME, VALID_PRODUCT_PRICE, VALID_PRODUCT_DESCRIPTION,
			VALID_START_TIME, VALID_START_TIME.plusMinutes(10))  // 15분 미만
		).isInstanceOf(CustomException.class);
	}

	@Test
	void 타임딜_스케줄링_성공() {
		assertThat(validProduct.getTimeDealSchedule()).isNotNull();
	}

	@Test
	void updateTimeDeal_성공() {
		// PENDING_SALE 상태에서 타임딜 수정
		ReflectionTestUtils.setField(validProduct, "status", ProductStatus.PENDING_SALE);

		LocalDateTime newStart = LocalDateTime.now().plusHours(2);
		LocalDateTime newEnd = LocalDateTime.now().plusHours(4);
		validProduct.updateTimeDeal(newStart, newEnd);

		assertThat(validProduct.getTimeDealSchedule().getStartTime()).isEqualTo(newStart);
		assertThat(validProduct.getTimeDealSchedule().getEndTime()).isEqualTo(newEnd);
		assertThat(validProduct.getStatus()).isEqualTo(ProductStatus.PENDING_RESERVATION);
	}

	@Test
	void updateTimeDeal_잘못된_상태() {
		// PENDING_RESERVATION 이외 상태에서 수정 불가
		ReflectionTestUtils.setField(validProduct, "status", ProductStatus.RESERVING);

		assertThatThrownBy(() -> validProduct.updateTimeDeal(
			LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4))
		).isInstanceOf(CustomException.class);
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
		assertThatThrownBy(() -> validProduct.complete())
			.isInstanceOf(CustomException.class);
	}

	@Test
	void cancelSale() {
		validProduct.cancelSale();
		assertThat(validProduct.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
	}

	@Test
	void updateInfo_성공() {
		validProduct.updateInfo("new", null, null);
		assertThat(validProduct.getName()).isEqualTo("new");
		assertThat(validProduct.getPrice()).isEqualTo(VALID_PRODUCT_PRICE);
		assertThat(validProduct.getDescription()).isEqualTo(VALID_PRODUCT_DESCRIPTION);
	}

	@Test
	void deleteProduct() {
		UUID id = UUID.randomUUID();
		validProduct.deleteProduct(id);
		assertThat(validProduct.getDeletedBy()).isEqualTo(id);
		assertNotNull(validProduct.getDeletedAt());
		assertThat(validProduct.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
	}
}