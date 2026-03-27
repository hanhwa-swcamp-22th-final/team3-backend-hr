package com.ohgiraffers.team3backendhr.config;

import com.ohgiraffers.team3backendhr.jwt.JwtAuthenticationFilter;
import com.ohgiraffers.team3backendhr.jwt.JwtTokenProvider;
import com.ohgiraffers.team3backendhr.jwt.RestAccessDeniedHandler;
import com.ohgiraffers.team3backendhr.jwt.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception ->
                    exception.authenticationEntryPoint(restAuthenticationEntryPoint)
                             .accessDeniedHandler(restAccessDeniedHandler))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
            .addFilterBefore(
                    new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
