package org.pgsg.product.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.pgsg.common.exception.CustomException;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeDealSchedule {
	private LocalDateTime startTime;
	private LocalDateTime endTime;

	public static TimeDealSchedule of(LocalDateTime startTime, LocalDateTime endTime) {
		TimeDealSchedule s = new TimeDealSchedule(startTime, endTime);
		s.validateTimeDealSchedule();
		return s;
	}

	private TimeDealSchedule(LocalDateTime startTime, LocalDateTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void validateTimeDealSchedule() {
		// Objects.requireNonNull(startTime,"시작 시간이 누락되었습니다.");	//todo: 누락 시 현재 시각으로 설정할 지 고려
		Objects.requireNonNull(endTime,"종료 시간이 누락되었습니다.");

		// if(startTime.isBefore(LocalDateTime.now()))	//todo: 타임딜 스케줄링 고도화 시 사용
		// 	throw new CustomException("StartTimeValidateException","startTime");

		if (endTime.isBefore(startTime))
			throw new CustomException("EndTimeValidateException","endTime");
	}
}
