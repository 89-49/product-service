package org.pgsg.product.config.security;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		UserContext.clear();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			log.warn("인증되지 않은 요청입니다. Path: {}", request.getRequestURI());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authentication");
			return false;
		}

		try {
			String principalName = authentication.getName();	//유저 식별값 - 여기서는 UUID
			String role = authentication.getAuthorities().stream()
				.findFirst()
				.map(auth->auth.getAuthority())
				.orElse("ROLE_USER");

			UserContext.setUserId(UUID.fromString(principalName));
			UserContext.setUserRole(role);

			MDC.put("userId", principalName);

			return true;
		} catch (IllegalArgumentException e) {
			log.error("유효하지 않은 유저 식별자 형식입니다.. Path: {}", request.getRequestURI());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid user identification");
			return false;
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		UserContext.clear();
		MDC.remove("userId");
	}
}
