package org.pgsg.product.global.exception;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ProductErrorCodeTest {
	@Test
	void validateYamlErrorKeys() throws Exception {
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(new ClassPathResource("application-product-error.yml"));
		Properties props = factory.getObject();

		for (ProductErrorCode exception : ProductErrorCode.values()) {
			String expectedKey = "error.configs[" + exception.getErrorKey() + "].code";

			assertThat(props.containsKey(expectedKey))
				.withFailMessage("YAML 파일에 정의되지 않은 에러 키: %s", expectedKey)
				.isTrue();
		}
	}
}