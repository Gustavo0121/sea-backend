package com.sea.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * No explicit AuthenticationProvider is wired here: with a single UserDetailsService bean
 * (UsuarioDetailsService) and a single PasswordEncoder bean (PasswordEncoderConfig) on the
 * classpath, Spring Boot's autoconfiguration builds the DaoAuthenticationProvider used by
 * the AuthenticationManager below automatically.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final RestAuthErrorHandler restAuthErrorHandler;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                           RestAuthErrorHandler restAuthErrorHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.restAuthErrorHandler = restAuthErrorHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthErrorHandler)
                        .accessDeniedHandler(restAuthErrorHandler)
                )
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .cacheControl(Customizer.withDefaults())
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'"))
                )
                .authorizeRequests(auth -> auth
                        .antMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .antMatchers(HttpMethod.GET, "/enderecos/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/clientes/**").hasAnyRole("ADMIN", "USER")
                        .antMatchers(HttpMethod.POST, "/clientes/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.PUT, "/clientes/**").hasRole("ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/clientes/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
