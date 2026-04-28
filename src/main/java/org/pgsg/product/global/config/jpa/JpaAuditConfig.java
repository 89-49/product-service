package org.pgsg.product.global.config.jpa;

import java.util.Optional;
import java.util.UUID;

import org.pgsg.product.global.config.security.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

	private static final UUID SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	@Bean
	public AuditorAware<UUID> auditorAware() {
		return () -> {
			UUID userId = UserContext.getUserId();

			if (userId == null)
				return Optional.of(SYSTEM_ID);

			return Optional.of(userId);
		};
	}
}
