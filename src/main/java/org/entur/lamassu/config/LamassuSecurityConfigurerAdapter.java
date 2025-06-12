package org.entur.lamassu.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class LamassuSecurityConfigurerAdapter {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    // State-less session (state in access token only)
    http.sessionManagement(sessionManagement ->
      sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    // Disable CSRF because of state-less session-management
    http.csrf(AbstractHttpConfigurer::disable);

    // Return 401 (unauthorized) instead of 302 (redirect to login) when
    // authorization is missing or invalid
    http.exceptionHandling(exceptionHandling ->
      exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
        response.addHeader(
          HttpHeaders.WWW_AUTHENTICATE,
          "Bearer realm=\"Restricted Content\""
        );
        response.sendError(
          HttpStatus.UNAUTHORIZED.value(),
          HttpStatus.UNAUTHORIZED.getReasonPhrase()
        );
      })
    );

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    // Configure authorization
    http.authorizeHttpRequests(authorize ->
      authorize
        .requestMatchers("/admin/**")
        .hasRole("admin")
        .requestMatchers("/**")
        .permitAll()
    );

    return http.build();
  }

  private UrlBasedCorsConfigurationSource corsConfigurationSource() {
    final var configuration = new CorsConfiguration();
    // should be restricted
    configuration.setAllowedOrigins(List.of("*"));
    configuration.setAllowedMethods(
      Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")
    );
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("*"));

    final var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
