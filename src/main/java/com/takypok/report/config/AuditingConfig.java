package com.takypok.report.config;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

@Configuration
public class AuditingConfig {
    @Bean
    public ReactiveAuditorAware<String> auditorAware() {
        return () ->
                ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .filter(Authentication::isAuthenticated)
                        .map(Authentication::getName)
                        .onErrorReturn("Anonymous")
                        .switchIfEmpty(
                                Mono.just("Anonymous"));
    }

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () ->
                Optional.of(
                        ZonedDateTime.now(
                                ZoneId.systemDefault()));
    }
}