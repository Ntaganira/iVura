package com.ntaganira.heritier.iVura.config;

import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler auditSuccessHandler,
                                                   AuthenticationFailureHandler auditFailureHandler,
                                                   LogoutSuccessHandler auditLogoutHandler,
                                                   AccessDeniedHandler auditAccessDeniedHandler) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                .requestMatchers("/patients/**", "/doctors/**", "/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                .requestMatchers("/departments/**", "/services/**", "/billings/**", "/users/**", "/roles/**", "/permissions/**").hasRole("ADMIN")
                .requestMatchers("/activity/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(auditSuccessHandler)
                .failureHandler(auditFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessHandler(auditLogoutHandler)
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(auditAccessDeniedHandler));
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> userRepo.findByUsername(username)
            .map(user -> org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(buildAuthorities(user))
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Builds authorities from a user's roles:
     * - ROLE_X from the role name itself
     * - PAGE_CODE for every page the role can access
     * - PERM_CODE for every action permission the role holds
     */
    private Set<SimpleGrantedAuthority> buildAuthorities(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (user.getRoles() == null) {
            return authorities;
        }
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority("PERM_" + p.getCode())));
            }
            if (role.getPages() != null) {
                role.getPages().forEach(p -> authorities.add(new SimpleGrantedAuthority("PAGE_" + p.getCode())));
            }
        });
        return authorities;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
