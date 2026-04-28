package org.pgsg.product.config.security;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {
		UserContext.clear();
		String userId = request.getHeader("X-User-Id");
		String userRole = request.getHeader("X-User-Role");

		if (userId == null || userId.isBlank()) {
			log.warn("인증 헤더가 누락되었습니다. Path: {}", request.getRequestURI());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-User-Id");
			return false;
		}

		try {
			UserContext.setUserId(UUID.fromString(userId));
			UserContext.setUserRole(userRole);

			MDC.put("userId", userId);

			return true;
		} catch (IllegalArgumentException e) {
			log.error("잘못된 UUID 형식의 헤더입니다. Path: {}", request.getRequestURI());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid X-User-Id format");
			return false;
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		UserContext.clear();
		MDC.remove("userId");
	}
}
