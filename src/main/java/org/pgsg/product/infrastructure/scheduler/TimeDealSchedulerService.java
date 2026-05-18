package org.pgsg.product.infrastructure.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealSchedulerService {

	private static final String GROUP = "timedeal";

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final Scheduler scheduler;

	/**
	 * 상품 등록 시 호출 — startTime에 TimeDealStartJob을 실행할 트리거를 등록한다. -> rescheduleTimeDealStart에 이미 신규 등록 분기가 있으므로 해당 분기에서만 사용
	 */
	private void scheduleTimeDealStart(UUID productId, LocalDateTime startTime) {
		try {
			JobDetail job = JobBuilder.newJob(TimeDealStartJob.class)
				.withIdentity(jobKey(productId))
				.usingJobData("productId", productId.toString())
				.storeDurably()
				.build();

			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(triggerKey(productId))
				.forJob(job)
				.startAt(toDate(startTime))
				.build();

			scheduler.scheduleJob(job, trigger);
			log.info("타임딜 스케줄 등록: productId={}, startTime={}", productId, startTime);

		} catch (SchedulerException e) {
			log.error("타임딜 스케줄 등록 실패: productId={}", productId, e);
			throw new RuntimeException("타임딜 스케줄 등록 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 타임딜 수정 시 호출 — 기존 트리거를 새 startTime으로 교체한다.
	 * 기존 스케줄이 없으면 신규 등록한다.
	 */
	public void rescheduleTimeDealStart(UUID productId, LocalDateTime newStartTime) {
		try {
			TriggerKey key = triggerKey(productId);

			if (scheduler.checkExists(key)) {
				Trigger newTrigger = TriggerBuilder.newTrigger()
					.withIdentity(key)
					.forJob(jobKey(productId))
					.startAt(toDate(newStartTime))
					.build();

				scheduler.rescheduleJob(key, newTrigger);
				log.info("타임딜 스케줄 수정: productId={}, newStartTime={}", productId, newStartTime);
			} else {
				// 스케줄이 없는 경우(예: 이전 실행 완료 후 재설정) 신규 등록
				Trigger newTrigger = TriggerBuilder.newTrigger()
					.withIdentity(triggerKey(productId))
					.forJob(jobKey(productId))
					.startAt(toDate(newStartTime))
					.build();

				if (scheduler.checkExists(jobKey(productId))) {
					scheduler.scheduleJob(newTrigger);  // Job은 유지, Trigger만 신규 등록
				} else {
					scheduleTimeDealStart(productId, newStartTime);  // Job + Trigger 모두 신규 등록
				}
				log.info("타임딜 스케줄 신규 등록: productId={}, newStartTime={}", productId, newStartTime);
			}

		} catch (SchedulerException e) {
			log.error("타임딜 스케줄 수정 실패: productId={}", productId, e);
			throw new RuntimeException("타임딜 스케줄 수정 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 판매 취소 등으로 스케줄을 제거할 때 호출한다.
	 */
	public void cancelSchedule(UUID productId) {
		try {
			scheduler.deleteJob(jobKey(productId));
			log.info("타임딜 스케줄 제거: productId={}", productId);
		} catch (SchedulerException e) {
			log.warn("타임딜 스케줄 제거 실패 (이미 실행됐을 수 있음): productId={}", productId, e);
			throw new RuntimeException("타임딜 스케줄 제거 중 오류가 발생했습니다.", e);
		}
	}

	private static JobKey jobKey(UUID productId) {
		return JobKey.jobKey("timedeal-" + productId, GROUP);
	}

	private static TriggerKey triggerKey(UUID productId) {
		return TriggerKey.triggerKey("trigger-" + productId, GROUP);
	}

	private static Date toDate(LocalDateTime ldt) {
		return Date.from(ldt.atZone(KST).toInstant());
	}
}