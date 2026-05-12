package org.pgsg.product.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Component("topicConfig")
@ConfigurationProperties(prefix = "topics")
@Getter
@Setter
public class TopicConfig {	//todo: 현재 필요한 부분만 우선 선언, 작동안되서 일단 배제하고 직접 문자열로 처리하는 방식으로 테스트 후 수정 확인 예정 - 현재 topics.yml파일 자체를 인식을 못하고 있는 듯함
	private Product product = new Product();
	private Trade trade = new Trade();
	private Reservation reservation = new Reservation();

	@Getter @Setter
	public static class Product {
		@NotBlank
		private String created;
		@NotBlank
		private String updated;
		@NotBlank
		private String deleted;
	}

	@Getter @Setter
	public static class Trade {
		@NotBlank
		private String completed;
	}

	@Getter @Setter
	public static class Reservation {
		@NotBlank
		private String completed;
		@NotBlank
		private String buyercancelled;
	}
}
