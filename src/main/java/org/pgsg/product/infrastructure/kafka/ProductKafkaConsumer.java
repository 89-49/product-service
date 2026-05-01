package org.pgsg.product.infrastructure.kafka;

import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.common.messaging.annotation.IdempotentConsumer;
import org.pgsg.product.application.service.ProductCommandService;
import org.pgsg.product.global.config.TopicConfig;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductKafkaConsumer {
	private final ProductCommandService productCommandService;
	//todo: 실제 이벤트 확인 후 Productevent 수정 예정

	//예약 취소 -> 판매 대기 중으로 변경 후 타임딜 등 재설정 대기
	@KafkaListener(topics = "#{topicConfig.reservation.cancelled}",groupId = "product-group")
	@IdempotentConsumer("product:reservation-cancelled")
	public void handleReservationCancelled(ConsumerRecord<String, OutboxEvent>record) {
		OutboxEvent event=record.value();
		UUID productId=event.correlationId();

		productCommandService.pendingSale(productId);
	}

	//예약 성공 -> 거래 중으로 상태 변경
	@KafkaListener(topics = "#{topicConfig.reservation.completed}",groupId = "product-group")
	@IdempotentConsumer("product:reservation-complete")
	public void handleReservationComplete(ConsumerRecord<String, OutboxEvent>record) {
		OutboxEvent event=record.value();
		UUID productId=event.correlationId();

		productCommandService.completeReservation(productId);
	}

	//거래 완료
	@KafkaListener(topics = "#{topicConfig.trade.completed}", groupId = "product-group")
	@IdempotentConsumer("product:trade-completed")
	public void handleTradeCompleted(ConsumerRecord<String, OutboxEvent>record) {
		OutboxEvent event=record.value();
		UUID productId=event.correlationId();

		productCommandService.completeTrade(productId);
	}
	//todo: 추가예정-취소 주체에 따른 세분화
}
