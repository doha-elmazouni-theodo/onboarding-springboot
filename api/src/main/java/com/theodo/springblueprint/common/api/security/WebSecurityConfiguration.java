package com.theodo.springblueprint.common.api.security;

import tools.jackson.databind.ObjectMapper;
import com.theodo.springblueprint.common.domain.ports.EventPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
// Necessary to use @PreAuthorize
// prePostEnabled by default is true.
@EnableMethodSecurity
@Configuration
public class WebSecurityConfiguration {

    public static final String PUBLIC_PATH_SEGMENT = "/public/";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain publicSecurityFilter(final HttpSecurity http) throws Exception {
        applyCommonSecurityConfig(http);
        http.securityMatcher(request -> request.getRequestURI().contains(PUBLIC_PATH_SEGMENT));
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain securedSecurityFilter(
        final HttpSecurity http,
        final StandaloneJwtAuthentication standaloneJwtAuthentication) throws Exception {
        applyCommonSecurityConfig(http);
        // disable request saving on authentication error
        // https://docs.spring.io/spring-security/reference/servlet/architecture.html#requestcache-prevent-saved-request
        http.requestCache(cache -> cache.requestCache(new NullRequestCache()));
        standaloneJwtAuthentication.configure(http);

        return http.build();
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private void applyCommonSecurityConfig(HttpSecurity http) throws Exception {
        // Enable CORS and disables CSRF
        http.cors(allowAllOrigins()).csrf(AbstractHttpConfigurer::disable);
        // Disable session creation
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Bean
    public AuthenticationProblemEntryPoint authenticationProblemEntryPoint(
        EventPublisherPort eventPublisher,
        ObjectMapper objectMapper) {
        return new AuthenticationProblemEntryPoint(eventPublisher, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<SimpleBasicAuthenticationFilter> managementFilter(
        @Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath,
        @Value("${management.server.user}") String username,
        @Value("${management.server.password}") String password) {
        FilterRegistrationBean<SimpleBasicAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SimpleBasicAuthenticationFilter("Actuator", username, password));
        registrationBean.setEnabled(true);
        registrationBean.addUrlPatterns(actuatorBasePath.replaceAll("/+$", "") + "/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // needed so that errors are returned as ProblemDetails JSON
        return registrationBean;
    }

    private static Customizer<CorsConfigurer<HttpSecurity>> allowAllOrigins() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return corsConfigurer -> corsConfigurer.configurationSource(source);
    }
}
