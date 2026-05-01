package org.pgsg.product.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component("topicConfig")
@ConfigurationProperties(prefix = "topics")
@Getter
public class TopicConfig {	//todo: 현재 필요한 부분만 우선 선언
	private Product product = new Product();
	private Trade trade = new Trade();

	@Getter
	public static class Product {
		private String created;
		private String updated;
		private String deleted;
	}

	@Getter
	public static class Trade {
		private String created;
		private String completed;
	}

	@Getter
	public static class reservation {
		private String cancelled;
	}
}
