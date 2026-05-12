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
import org.pgsg.product.application.mapper.ProductApplicationMapper;
import org.pgsg.product.domain.event.ProductCreatedEvent;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.model.ProductStatus;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.global.config.TopicConfig;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product 서비스 코드 테스트")
class ProductCommandServiceTest {
	@Mock
	private ProductRepository productRepository;
	@InjectMocks
	private ProductCommandService productCommandService;
	@Mock
	private ProductApplicationMapper mapper;
	@Mock
	private TopicConfig topicConfig;
	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Captor
	private ArgumentCaptor<Product> captor;

	private final String PRODUCT_NAME="testName";
	private final Integer PRICE=100;
	private final LocalDateTime END_TIME= LocalDateTime.now().plusHours(1);
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
		//given
		CreateProductCommand command = new CreateProductCommand(PRODUCT_NAME, PRICE, null);
		when(productRepository.save(any(Product.class)))
			.thenAnswer(invocation -> {
				Product p = invocation.getArgument(0);
				ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
				ReflectionTestUtils.setField(p, "name", PRODUCT_NAME);
				ReflectionTestUtils.setField(p, "price", PRICE);
				return p;
			});

		//when
		productCommandService.createProduct(command);

		//then
		verify(productRepository).save(captor.capture());
		Product saved=captor.getValue();
		assertThat(saved.getName()).isEqualTo(PRODUCT_NAME);
		assertThat(saved.getPrice()).isEqualTo(PRICE);
	}

	@Test
	void createProduct_실패_잘못된_가격(){
		assertThatThrownBy(()->productCommandService.createProduct(new CreateProductCommand(PRODUCT_NAME, -1, null)))
			.isInstanceOf(CustomException.class);
	}

	@Test
	void deleteProduct() {
		//given
		UUID userId=UUID.randomUUID();
		Product product = Product.create(PRODUCT_NAME, PRICE, null);
		securityUtilMockedStatic.when(SecurityUtil::getCurrentUserIdOrThrow).thenReturn(userId);
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

		//when
		productCommandService.deleteProduct(UUID.randomUUID());

		//then
		assertThat(product.getDeletedBy()).isNotNull();
	}

	@Test
	void updateProduct() {
		//given
		Product product = Product.create(PRODUCT_NAME, PRICE, null);
		UpdateProductCommand command=new UpdateProductCommand("test",1,null,LocalDateTime.now(),LocalDateTime.now().plusHours(1));
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
		when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
			Product p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
			ReflectionTestUtils.setField(p, "name", PRODUCT_NAME);
			ReflectionTestUtils.setField(p, "price", PRICE);
			return p;
		});

		//when
		productCommandService.updateProduct(UUID.randomUUID(),command);

		//then
		verify(productRepository).saveAndFlush(captor.capture());
		Product saved=captor.getValue();
		assertThat(saved.getName()).isEqualTo(PRODUCT_NAME);
		assertThat(saved.getPrice()).isEqualTo(PRICE);

	}

	@Test
	void 타임딜_설정(){
		//given
		Product product = Product.create(PRODUCT_NAME, PRICE, null);
		UpdateTimeDealCommand command=new UpdateTimeDealCommand(LocalDateTime.now().plusHours(1));

		TopicConfig.Product mockProduct = mock(TopicConfig.Product.class);
		when(topicConfig.getProduct()).thenReturn(mockProduct);
		when(mockProduct.getCreated()).thenReturn("prod-product-created");

		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
		when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> {
			Product p = invocation.getArgument(0);
			ReflectionTestUtils.setField(p, "id", UUID.randomUUID());
			ReflectionTestUtils.setField(p, "name", PRODUCT_NAME);
			ReflectionTestUtils.setField(p, "price", PRICE);
			return p;
		});
		when(mapper.toCreatedEvent(any(Product.class)))
			.thenReturn(new ProductCreatedEvent(
				UUID.randomUUID(),
				"testName",
				100,
				LocalDateTime.now().plusHours(1),
				UUID.randomUUID()

			));

		//when
		productCommandService.setTimeDeal(UUID.randomUUID(),command);

		//then
		verify(productRepository).saveAndFlush(captor.capture());
		Product saved=captor.getValue();
		assertThat(saved.getTimeDealSchedule()).isNotNull();
	}

	@Test
	void 판매취소(){
		//given
		Product product = Product.create(PRODUCT_NAME, PRICE, null);
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

		//when
		productCommandService.cancelSale(UUID.randomUUID());

		//then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
	}
}