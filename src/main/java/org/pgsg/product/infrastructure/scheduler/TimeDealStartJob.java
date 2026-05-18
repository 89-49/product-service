package org.pgsg.product.infrastructure.scheduler;

import java.util.UUID;

import org.pgsg.product.domain.repository.ProductRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 타임딜 startTime 도달 시 Quartz가 실행하는 Job.
 * infrastructure 계층에서 domain을 직접 호출하여 역방향 의존 방지.
 * 1. PENDING_RESERVATION → RESERVING 상태 전이 (startReserve)
 * 2. Outbox 경유 Kafka 이벤트 발행 (타임딜 시작 알림 — 예약 서비스 구독)
 * 두 작업이 같은 트랜잭션 내에서 처리되어 상태 전이와 이벤트 발행이 동시에 보장됨.
 */
@Slf4j
@Component
public class TimeDealStartJob implements Job {

	// Quartz는 Job 인스턴스를 직접 생성하므로 필드 주입 사용
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private TimeDealStartService timeDealStartService;

	@Override
	public void execute(JobExecutionContext context) {
		UUID productId = UUID.fromString(
			context.getMergedJobDataMap().getString("productId")
		);
		timeDealStartService.startTimeDeal(productId);
	}
}
