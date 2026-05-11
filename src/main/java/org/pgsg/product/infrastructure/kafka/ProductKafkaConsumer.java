package org.pgsg.product.infrastructure.kafka;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.pgsg.common.event.OutboxEvent;
import org.pgsg.common.exception.CustomException;
import org.pgsg.common.messaging.annotation.IdempotentConsumer;
import org.pgsg.common.util.JsonUtil;
import org.pgsg.product.application.service.ProductCommandService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductKafkaConsumer {
	private final ProductCommandService productCommandService;
	//todo: 실제 이벤트 확인 후 Productevent 수정 예정

	// //예약 취소 -> 판매 대기 중으로 변경 후 타임딜 등 재설정 대기	//todo: 예약 취소 구현 후 수정, yml 파일 읽는 방식은 현재 해당 파일 인식을 못하고 있어서 추후 다시 시도예정
	// @KafkaListener(topics = "#{topicConfig.reservation.cancelled}",groupId = "product-group")
	// @IdempotentConsumer("product:reservation-cancelled")
	// public void handleReservationCancelled(ConsumerRecord<String, OutboxEvent>record) {
	// 	OutboxEvent event=record.value();
	// 	UUID productId=event.correlationId();
	//
	// 	productCommandService.pendingSale(productId);
	// }

	//예약 성공 -> 거래 중으로 상태 변경
	@IdempotentConsumer("product:reservation-complete")
	@KafkaListener(topics = "${topics.reservation.completed}",groupId = "product-group")
	public void handleReservationComplete(ConsumerRecord<String, String>record, Acknowledgment ack) {
		UUID productId = extractProductId(record.value());
		if (productId == null) return;
		try {
			productCommandService.completeReservation(productId);
		} catch (CustomException e) {
			log.error("도메인 예외 발생 - 스킵 처리: productId={}, error={}", productId, e);
		}
	}

	//거래 완료
	@IdempotentConsumer("product:trade-completed")
	@KafkaListener(topics = "${topics.trade.completed}", groupId = "product-group")
	public void handleTradeCompleted(ConsumerRecord<String, String>record, Acknowledgment ack) {
		UUID productId = extractProductId(record.value());
		if (productId == null) return;
		try {
			productCommandService.completeTrade(productId);
		} catch (CustomException e) {
			log.error("도메인 예외 발생 - 스킵 처리: productId={}, error={}", productId, e);
		}

	}
	//todo: 추가예정-취소 주체에 따른 세분화

	private UUID extractProductId(String value) {
		try {
			Map<String, Object> map = JsonUtil.fromJson(value, new TypeReference<>() {
			});
			if (map == null || !map.containsKey("productId")) {	//todo: correlationId->productId 나중에 검토
				log.error("productId 누락");
				return null;
			}
			return UUID.fromString((String) map.get("productId"));
		} catch (IllegalArgumentException e) {
			log.error("유효하지 않은 UUID 형식: {}", e);
			return null;
		} catch (Exception e) {
			log.error("메시지 파싱 실패: {}", e);
			return null;
		}
	}
}
