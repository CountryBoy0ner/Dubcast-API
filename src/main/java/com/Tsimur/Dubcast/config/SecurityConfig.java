package com.Tsimur.Dubcast.config;

import com.Tsimur.Dubcast.exception.handler.RestAccessDeniedHandler;
import com.Tsimur.Dubcast.exception.handler.RestAuthEntryPoint;
import com.Tsimur.Dubcast.model.Role;
import com.Tsimur.Dubcast.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RestAuthEntryPoint restAuthEntryPoint;
  private final RestAccessDeniedHandler restAccessDeniedHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/**")
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(restAuthEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
                    .permitAll()
                    .requestMatchers("/radio-ws/**")
                    .permitAll() // SockJS/WebSocket handshake
                    .requestMatchers(ApiPaths.AUTH + "/**")
                    .permitAll()
                    .requestMatchers(ApiPaths.RADIO + "/**")
                    .permitAll()
                    .requestMatchers(ApiPaths.PROGRAMMING + "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.CHAT + "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.PROFILE + "/public/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.PLAYLIST + "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.LIKES + "/**")
                    .hasAnyAuthority(Role.ROLE_USER.name(), Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.DELETE, ApiPaths.LIKES + "/**")
                    .hasAnyAuthority(Role.ROLE_USER.name(), Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.GET, ApiPaths.LIKES + "/**")
                    .hasAnyAuthority(Role.ROLE_USER.name(), Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.POST, ApiPaths.CHAT + "/**")
                    .hasAnyAuthority(Role.ROLE_USER.name(), Role.ROLE_ADMIN.name())
                    .requestMatchers(
                        ApiPaths.ADMIN + "/**",
                        ApiPaths.USERS + "/**",
                        ApiPaths.TRACK + "/**",
                        ApiPaths.SCHEDULE + "/**",
                        ApiPaths.PLAYLIST + "/**")
                    .hasAuthority(Role.ROLE_ADMIN.name())
                    .requestMatchers(ApiPaths.PROFILE + "/**")
                    .hasAnyAuthority(Role.ROLE_USER.name(), Role.ROLE_ADMIN.name())
                    .anyRequest()
                    .denyAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
