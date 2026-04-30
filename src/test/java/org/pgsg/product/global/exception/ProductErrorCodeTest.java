package org.pgsg.product.global.exception;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class ProductErrorCodeTest {
	@Test
	void validateYamlErrorKeys() throws Exception {
		// 1. YAML 파일 로드 (application-product-error.yml 파일명에 맞게 수정하세요)
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		List<PropertySource<?>> productErrorYaml = loader.load("product-error", new ClassPathResource("application-product-error.yml"));
		List<PropertySource<?>> commonErrorYaml = loader.load("common-error", new ClassPathResource("application-error.yml"));

		// 2. Enum의 모든 키 가져오기
		// getErrorKey()가 "product.resource.not-found.product" 같은 문자열을 반환한다고 가정
		for (ProductErrorCode exception : ProductErrorCode.values()) {
			String expectedKey = "error.configs." + exception.getErrorKey();

			// 3. YAML에 해당 키가 존재하는지 검증
			boolean existsInProduct = productErrorYaml.stream().anyMatch(ps -> ps.containsProperty(expectedKey));
			boolean existsInCommon = commonErrorYaml.stream().anyMatch(ps -> ps.containsProperty(expectedKey));

			assertThat(existsInProduct || existsInCommon)
				.withFailMessage("YAML 파일에 정의되지 않은 에러 키가 존재합니다: " + expectedKey)
				.isTrue();
		}
	}
}