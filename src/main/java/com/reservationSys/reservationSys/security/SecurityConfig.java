package com.reservationSys.reservationSys.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint CustomAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler CustomAccessDeniedHandler;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserStatusChecker userStatusChecker;
    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler, JwtAuthFilter jwtAuthFilter, UserStatusChecker userStatusChecker) {
        CustomAuthenticationEntryPoint = customAuthenticationEntryPoint;
        CustomAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtAuthFilter = jwtAuthFilter;
        this.userStatusChecker = userStatusChecker;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.
                csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .exceptionHandling(
                        exceptionHandling->exceptionHandling.authenticationEntryPoint(CustomAuthenticationEntryPoint)
                                .accessDeniedHandler(CustomAccessDeniedHandler)
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/api/v1/auth/register","/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/verify-email","/api/v1/auth/resend-verification-email","/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/api/v1/auth/verify-phone","/api/v1/auth/resend-verification-phone")
                                .access(
                                        (authSupplier,context)->{
                                            Authentication authentication = authSupplier.get();
                                            boolean allowed = authentication.isAuthenticated() && userStatusChecker.isEmailVerified(authentication);
                                            return new AuthorizationDecision(allowed);
                                        }
                                )
                                .anyRequest().access(
                                        (authSupplier, context) -> {
                                            Authentication authentication = authSupplier.get();
                                            boolean allowed = authentication.isAuthenticated() && userStatusChecker.isActive(authentication);
                                            return new AuthorizationDecision(allowed);
                                        }
                                )
                )
                .sessionManagement(sess->sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();

    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // Vite default port
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
