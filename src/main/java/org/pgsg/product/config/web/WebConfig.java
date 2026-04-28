package org.pgsg.product.config.web;

import org.pgsg.product.config.security.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final UserContextInterceptor userContextInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
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
