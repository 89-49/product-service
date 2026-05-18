package org.pgsg.product.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.util.SecurityUtil;
import org.pgsg.config.security.UserDetailsImpl;
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.dto.command.UpdateProductCommand;
import org.pgsg.product.application.dto.command.UpdateTimeDealCommand;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.model.ProductStatus;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.infrastructure.scheduler.TimeDealSchedulerService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product 서비스 코드 테스트")
class ProductCommandServiceTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private TimeDealSchedulerService schedulerService;
	@InjectMocks
	private ProductCommandService productCommandService;

	@Captor
	private ArgumentCaptor<Product> captor;

	private static final String PRODUCT_NAME = "testName";
	private static final Integer PRICE = 100;
	private static final LocalDateTime START_TIME = LocalDateTime.now().plusHours(1);
	private static final LocalDateTime END_TIME = LocalDateTime.now().plusHours(3);

	private MockedStatic<SecurityUtil> securityUtilMockedStatic;

	@BeforeEach
	void setUp() {
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
	void createProduct_성공() {
		// given
		CreateProductCommand command = new CreateProductCommand(PRODUCT_NAME, PRICE, null, START_TIME, END_TIME);
		when(productRepository.save(any(Product.class)))
			.thenAnswer(invocation -> {
				Product p = invocation.getArgument(0);
				ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
				return p;
			});

		// when
		productCommandService.createProduct(command);

		// then
		verify(productRepository).save(captor.capture());
		Product saved = captor.getValue();
		assertThat(saved.getName()).isEqualTo(PRODUCT_NAME);
		assertThat(saved.getPrice()).isEqualTo(PRICE);
		assertThat(saved.getTimeDealSchedule()).isNotNull();
		assertThat(saved.getStatus()).isEqualTo(ProductStatus.PENDING_RESERVATION);

		// 스케줄 등록 호출 검증
		verify(schedulerService).rescheduleTimeDealStart(any(UUID.class), eq(START_TIME));
	}

	@Test
	void createProduct_실패_잘못된_가격() {
		assertThatThrownBy(() -> productCommandService.createProduct(
			new CreateProductCommand(PRODUCT_NAME, -1, null, START_TIME, END_TIME))
		).isInstanceOf(CustomException.class);
	}

	@Test
	void deleteProduct() {
		// given
		UUID userId = UUID.randomUUID();
		Product product = Product.create(PRODUCT_NAME, PRICE, null, START_TIME, END_TIME);
		ReflectionTestUtils.setField(product, "createdBy", userId);
		securityUtilMockedStatic.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(userId); // 추가
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

		// when
		productCommandService.deleteProduct(UUID.randomUUID());

		// then
		assertThat(product.getDeletedBy()).isNotNull();
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
		verify(schedulerService).cancelSchedule(any(UUID.class));
	}

	@Test
	void updateInfoProduct() {
		// given
		Product product = Product.create(PRODUCT_NAME, PRICE, null, START_TIME, END_TIME);
		UpdateProductCommand command = new UpdateProductCommand("newName", 200, "newDesc");
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
		when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		productCommandService.updateProduct(UUID.randomUUID(), command);

		// then
		verify(productRepository).saveAndFlush(captor.capture());
		Product saved = captor.getValue();
		assertThat(saved.getName()).isEqualTo("newName");
		assertThat(saved.getPrice()).isEqualTo(200);
	}

	@Test
	void 타임딜_수정_성공() {
		// given
		Product product = Product.create(PRODUCT_NAME, PRICE, null, START_TIME, END_TIME);
		// PENDING_SALE 상태로 설정 (타임딜 수정 가능 상태)
		ReflectionTestUtils.setField(product, "status", ProductStatus.PENDING_SALE);

		LocalDateTime newStart = LocalDateTime.now().plusHours(2);
		LocalDateTime newEnd = LocalDateTime.now().plusHours(4);
		UpdateTimeDealCommand command = new UpdateTimeDealCommand(newStart, newEnd);

		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
		when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation ->{
			Product p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", UUID.randomUUID()); // id 설정 추가
			return p;
		});

		// when
		productCommandService.updateTimeDeal(UUID.randomUUID(), command);

		// then
		verify(productRepository).saveAndFlush(captor.capture());
		Product saved = captor.getValue();
		assertThat(saved.getTimeDealSchedule().getStartTime()).isEqualTo(newStart);
		assertThat(saved.getTimeDealSchedule().getEndTime()).isEqualTo(newEnd);
		assertThat(saved.getStatus()).isEqualTo(ProductStatus.PENDING_RESERVATION);
		verify(schedulerService).rescheduleTimeDealStart(any(UUID.class), any(LocalDateTime.class));
	}

	@Test
	void 판매취소() {
		// given
		Product product = Product.create(PRODUCT_NAME, PRICE, null, START_TIME, END_TIME);
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

		// when
		productCommandService.cancelSale(UUID.randomUUID());

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
		verify(schedulerService).cancelSchedule(any(UUID.class));
	}
}