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
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.dto.command.UpdateProductCommand;
import org.pgsg.product.application.dto.command.UpdateTimeDealCommand;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.model.ProductStatus;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.global.config.security.UserContext;
import org.pgsg.product.presentation.dto.request.UpdateProductRequest;
import org.springframework.security.core.parameters.P;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product 서비스 코드 테스트")
class ProductCommandServiceTest {
	@Mock
	private ProductRepository productRepository;
	@InjectMocks
	private ProductCommandService productCommandService;

	@Captor
	private ArgumentCaptor<Product> captor;

	private final String PRODUCT_NAME="testName";
	private final Integer PRICE=100;
	private final LocalDateTime END_TIME= LocalDateTime.now().plusHours(1);
	private MockedStatic<UserContext> userContextMock;

	@BeforeEach
	void setUp() {
		if (userContextMock != null)
			userContextMock.close();

		userContextMock = mockStatic(UserContext.class);
	}

	@AfterEach
	void tearDown() {
		userContextMock.close(); // 반드시 닫아줘야 함!
	}

	@Test
	void createProduct_성공() {
		//given
		CreateProductCommand command = new CreateProductCommand(PRODUCT_NAME, PRICE, null);
		when(productRepository.save(any(Product.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

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
		userContextMock.when(UserContext::getUserId).thenReturn(userId);
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));

		//when
		productCommandService.deleteProduct(UUID.randomUUID());

		//then
		assertThat(product.getDeletedBy()).isEqualTo(userId);
	}

	@Test
	void updateProduct() {
		//given
		Product product = Product.create(PRODUCT_NAME, PRICE, null);
		UpdateProductCommand command=new UpdateProductCommand("test",1,null,LocalDateTime.now(),LocalDateTime.now().plusHours(1));
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
		when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

		//when
		productCommandService.updateProduct(UUID.randomUUID(),command);

		//then
		verify(productRepository).saveAndFlush(captor.capture());
		Product saved=captor.getValue();
		assertThat(saved.getName()).isEqualTo("test");
		assertThat(saved.getPrice()).isEqualTo(1);

	}

	@Test
	void 타임딜_설정(){
		//given
		Product product = Product.create(PRODUCT_NAME, PRICE, null);
		UpdateTimeDealCommand command=new UpdateTimeDealCommand(LocalDateTime.now().plusHours(1));
		when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(product));
		when(productRepository.saveAndFlush(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
		productCommandService.cancelSaleProduct(UUID.randomUUID());

		//then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SALE_CANCELED);
	}
}