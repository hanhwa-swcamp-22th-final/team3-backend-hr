package com.ohgiraffers.team3backendhr.config;

import com.ohgiraffers.team3backendhr.auth.command.application.dto.EmployeeUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof EmployeeUserDetails userDetails) {
            return Optional.of(userDetails.getEmployeeId());
        }

        return Optional.empty();
    }
}
