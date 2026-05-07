package org.pgsg.product.global.config.security;

import org.pgsg.config.security.CustomAccessDeniedHandler;
import org.pgsg.config.security.CustomAuthenticationEntryPoint;
import org.pgsg.config.security.LoginFilter;
import org.pgsg.config.security.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ProductSecurityConfig implements SecurityConfig {

	private final LoginFilter loginFilter;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(c
				-> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/internal/v1/**").permitAll()
				.requestMatchers("/favicon.ico", "/error").permitAll()
				.requestMatchers("/actuator/health", "/actuator/info").permitAll()
				.anyRequest().authenticated()
			)

			.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(c -> {
				c.authenticationEntryPoint(authenticationEntryPoint);
				c.accessDeniedHandler(accessDeniedHandler);
			});

		return http.build();
	}
}