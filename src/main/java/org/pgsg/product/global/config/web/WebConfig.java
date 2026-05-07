package org.pgsg.product.global.config.web;

import java.util.Arrays;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final UserContextInterceptor userContextInterceptor;
	private final Environment env;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if(!Arrays.asList(env.getActiveProfiles()).contains("local"))	//todo: 로컬테스트용
		registry.addInterceptor(userContextInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(
				"/health",
				"/actuator/**",
				"/v3/api-docs/**",
				"/swagger-ui/**"
			);
	}
}
