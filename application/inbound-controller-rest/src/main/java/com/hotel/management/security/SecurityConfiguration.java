package com.hotel.management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    private final RestSecurityExceptionHandler restSecurityExceptionHandler;

    SecurityConfiguration(RestSecurityExceptionHandler restSecurityExceptionHandler) {
        this.restSecurityExceptionHandler = restSecurityExceptionHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(this::configureAuthorizationRules)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler))
                .oauth2ResourceServer(oauth2 -> configureOauth2ResourceServer(oauth2, jwtDecoder))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }

    private void configureAuthorizationRules(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/actuator/prometheus").permitAll()
                .requestMatchers(HttpMethod.POST, "/admin/hotels").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/admin/hotels/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/room-types").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/admin/room-types/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/rooms").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/admin/rooms/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/hotels/*/services").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/admin/hotels/*/services/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/admin/hotels/*/services/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/admin/staff").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/admin/staff/*/hotel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/admin/staff/*/hotel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/hotels", "/hotels/*", "/hotels/*/services", "/hotels/*/room-types").permitAll()
                .requestMatchers(HttpMethod.GET, "/hotels/*/room-types/*/availability-calendar").permitAll()
                .requestMatchers(HttpMethod.POST, "/public/reservations").permitAll()
                .requestMatchers(HttpMethod.POST, "/rooms/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/me/reservations").hasRole("GUEST")
                .requestMatchers(HttpMethod.POST, "/reservations").hasRole("GUEST")
                .requestMatchers(HttpMethod.GET, "/reservations").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/reservations/*/cancel").hasAnyRole("GUEST", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/staff/reservations").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/staff/reservations/*/check-in").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/staff/reservations/*/check-out").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/staff/reservations/*/no-show").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET, "/staff/rooms").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PATCH, "/staff/rooms/*/status").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET, "/reservations/*").hasAnyRole("GUEST", "ADMIN", "STAFF")
                .anyRequest().authenticated();
    }

    private void configureOauth2ResourceServer(OAuth2ResourceServerConfigurer<HttpSecurity> oauth2, JwtDecoder jwtDecoder) {
        oauth2.jwt(jwt -> {
            jwt.decoder(jwtDecoder);
            jwt.jwtAuthenticationConverter(JwtConverter::new);
        });
    }
}
