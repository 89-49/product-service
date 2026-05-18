package org.pgsg.product.infrastructure.scheduler;

import java.util.UUID;

import org.pgsg.common.event.OutboxEvent;
import org.pgsg.common.exception.CustomException;
import org.pgsg.product.application.mapper.ProductApplicationMapper;
import org.pgsg.product.domain.event.ProductCreatedEvent;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.global.config.TopicConfig;
import org.pgsg.product.global.exception.ProductErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TimeDealStartJob에서 위임받아 트랜잭션 내에서 상태 전이와 이벤트 발행을 처리한다.
 * Quartz Job은 Spring 컨텍스트 외부에서 생성되므로 @Transactional이 적용되지 않는다.
 * 트랜잭션이 필요한 로직은 이 Spring 관리 서비스에 위임한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealStartService {

	private final ProductRepository productRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final TopicConfig topicConfig;
	private final ProductApplicationMapper mapper;

	@Transactional
	public void startTimeDeal(UUID productId) {
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new CustomException(ProductErrorCode.ProductNotFoundException));

		product.startReserve();  // PENDING_RESERVATION → RESERVING
		productRepository.save(product);

		ProductCreatedEvent payload = mapper.toCreatedEvent(product);
		String eventType = topicConfig.getProduct().getCreated();
		OutboxEvent event = new OutboxEvent(productId, productId, "Product", eventType, payload);
		eventPublisher.publishEvent(event);

		log.info("타임딜 시작: productId={}, eventType={}", productId, eventType);
	}
}