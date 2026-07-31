package com.ntaganira.heritier.iVura.config;

import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.config
 * - File      : AuditSecurityConfig.java
 * - Date      : 2026. 08. 01.
 * - User      : Hntaganira
 * - Desc      : Records logins, failed logins, logouts and access-denied attempts
 * </pre>
 */
@Configuration
public class AuditSecurityConfig {

    @Bean
    public AuthenticationSuccessHandler auditSuccessHandler(ActivityLogService activityLogService) {
        return (request, response, authentication) -> {
            activityLogService.record(authentication.getName(), "Authentication", "LOGIN",
                    "Login successful", ActivityStatus.SUCCESS, request.getRemoteAddr());
            response.sendRedirect("/dashboard");
        };
    }

    @Bean
    public AuthenticationFailureHandler auditFailureHandler(ActivityLogService activityLogService) {
        return (request, response, exception) -> {
            activityLogService.record(request.getParameter("username"), "Authentication", "LOGIN_FAILED",
                    "Failed login attempt", ActivityStatus.FAILED, request.getRemoteAddr());
            response.sendRedirect("/login?error");
        };
    }

    @Bean
    public LogoutSuccessHandler auditLogoutHandler(ActivityLogService activityLogService) {
        return (request, response, authentication) -> {
            String username = authentication != null ? authentication.getName() : null;
            activityLogService.record(username, "Authentication", "LOGOUT",
                    "User logged out", ActivityStatus.SUCCESS, request.getRemoteAddr());
            response.sendRedirect("/login?logout");
        };
    }

    @Bean
    public AccessDeniedHandler auditAccessDeniedHandler(ActivityLogService activityLogService) {
        return (request, response, accessDeniedException) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            activityLogService.record(username, "Access Control", "ACCESS_DENIED",
                    "Denied access to " + request.getRequestURI(), ActivityStatus.FAILED,
                    request.getRemoteAddr());
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
        };
    }
}
