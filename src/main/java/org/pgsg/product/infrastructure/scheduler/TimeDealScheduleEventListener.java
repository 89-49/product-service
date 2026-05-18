package org.pgsg.product.application.event;

import org.pgsg.product.domain.event.TimeDealScheduleEvent;
import org.pgsg.product.infrastructure.scheduler.TimeDealSchedulerService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DB 커밋 완료 후 Quartz 스케줄을 등록/교체한다.
 * AFTER_COMMIT 단계에서 실행되므로 트랜잭션 롤백 시 스케줄이 등록되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeDealScheduleEventListener {

	private final TimeDealSchedulerService schedulerService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(TimeDealScheduleEvent event) {
		schedulerService.rescheduleTimeDealStart(event.productId(), event.startTime());
		log.info("타임딜 스케줄 등록 완료 (커밋 후): productId={}, startTime={}",
			event.productId(), event.startTime());
	}
}