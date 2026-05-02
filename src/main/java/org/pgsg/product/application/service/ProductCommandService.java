package org.pgsg.product.application.service;

import static org.pgsg.product.global.exception.ProductErrorCode.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.pgsg.common.event.OutboxEvent;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.util.JsonUtil;
import org.pgsg.product.application.dto.command.CreateProductCommand;
import org.pgsg.product.application.dto.command.UpdateProductCommand;
import org.pgsg.product.application.dto.command.UpdateTimeDealCommand;
import org.pgsg.product.application.dto.result.CreateProductResult;
import org.pgsg.product.application.dto.result.UpdateProductResult;
import org.pgsg.product.application.mapper.ProductApplicationMapper;
import org.pgsg.product.domain.event.ProductCreatedEvent;
import org.pgsg.product.domain.model.Product;
import org.pgsg.product.domain.model.TimeDealSchedule;
import org.pgsg.product.domain.repository.ProductRepository;
import org.pgsg.product.global.config.TopicConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
* 상품 추가, 삭제, 수정 조회, 이벤트 연결
* */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductCommandService {
	private final ProductRepository productRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ProductApplicationMapper mapper;
	private final TopicConfig topicConfig;

	public CreateProductResult createProduct(CreateProductCommand command) {
		Product product =Product.create(
			command.name(),command.price(),command.description());	//todo: 스케줄 입력 시기 변경 후 수정 필요

		Product saved=productRepository.save(product);
		//todo: 스케줄링 도구 적용 시 생성 후 스케줄이 등록되도록 고도화 예정
		return new CreateProductResult(saved.getName(),saved.getPrice(),saved.getDescription());
	}

	public void deleteProduct(UUID id) {
		Product product=findById(id);

		UUID userId = /*Objects.requireNonNull(UserContext.getUserId(), "인증 사용자 정보가 없습니다.");*/
			UUID.randomUUID();	//todo: 로컬 테스트용, 인증 서비스 연결 후 수정
		product.deleteProduct(userId);
	}

	public UpdateProductResult updateProduct(UUID id, UpdateProductCommand command) {
		Product product = findById(id);

		//todo: timeDealSchedule 설정부분 리팩토링 후 수정 예정
		TimeDealSchedule newSchedule = command.endTime() == null ? null
			: TimeDealSchedule.of(command.startTime() == null ? LocalDateTime.now() : command.startTime(),
			command.endTime());

		product.update(command.name(), command.price(), command.description(), newSchedule);

		Product saved = productRepository.saveAndFlush(product);

		TimeDealSchedule schedule = saved.getTimeDealSchedule();
		return new UpdateProductResult(
			saved.getName(),
			saved.getPrice(),
			saved.getDescription(),
			schedule == null ? null : schedule.getStartTime(),
			schedule == null ? null : schedule.getEndTime()
		);
	}
	public UpdateProductResult setTimeDeal(UUID id, UpdateTimeDealCommand command) {
		Product product = findById(id);

		product.setTimeDealSchedule(command.endTime());

		Product saved=productRepository.saveAndFlush(product);

		//todo: mvp 이후 이벤트 발행 위치 변경 예정
		ProductCreatedEvent payload = mapper.toCreatedEvent(product);
		System.out.println("1. payload 생성 완료");
		String jsonPayload=JsonUtil.toJson(payload);
		System.out.println("2. jsonPayload 생성 완료: {}");
		String eventType=topicConfig.getProduct().getCreated();
		System.out.println("3. eventType: {}");
		OutboxEvent event=new OutboxEvent(saved.getId(),  saved.getId(),"Product", eventType, jsonPayload);
		System.out.println("4. OutboxEvent 생성 완료");

		System.out.println("Kafka 이벤트 발행 시도: eventType={}, productId={}");
		kafkaTemplate.send(eventType,event)
			.whenComplete((result, ex) -> {
				if (ex == null) {
					log.info("Kafka 전송 성공: topic={}, offset={}",
						result.getRecordMetadata().topic(),
						result.getRecordMetadata().offset());
				} else {
					log.error("Kafka 전송 실패: {}", ex.getMessage(), ex);
				}
			});;

		return new UpdateProductResult(saved.getName(), saved.getPrice(), saved.getDescription(),
			saved.getTimeDealSchedule().getStartTime(), saved.getTimeDealSchedule().getEndTime());
	}

	public void cancelSale(UUID id) {
		Product product = findById(id);
		product.cancelSale();

		// UUID userId = /*Objects.requireNonNull(UserContext.getUserId(), "인증 사용자 정보가 없습니다.");*/
		// 	UUID.fromString("00000000-0000-0000-0000-000000000000");	//todo: 로컬 테스트용, 인증 서비스 연결 후 수정
		// product.deleteProduct(id);	//todo: 삭제와 판매 취소를 동일하게 할지 좀 더 고려
	}

	public void completeTrade(UUID id) {
		Product product = findById(id);
		product.complete();
	}

	public void completeReservation(UUID id) {
		Product product = findById(id);
		product.startTrade();
	}

	public void pendingSale(UUID id) {
		Product product = findById(id);
		product.revertToReserving();
	}


	private Product findById(UUID id) {
		return productRepository.findById(id)
			.orElseThrow(()->new CustomException(ProductNotFoundException));
	}
}
