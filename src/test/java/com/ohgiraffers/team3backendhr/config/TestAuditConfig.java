package com.ohgiraffers.team3backendhr.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA auditing 환경에서 @DataJpaTest가 created_by(NOT NULL)를 null로 저장하는 것을 방지.
 * repository 단위 테스트에서 @Import(TestAuditConfig.class)로 포함시켜 사용.
 */
@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class TestAuditConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.of(0L);
    }
}
