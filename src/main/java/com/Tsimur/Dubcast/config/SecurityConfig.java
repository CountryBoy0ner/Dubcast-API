package com.Tsimur.Dubcast.config;

import com.Tsimur.Dubcast.exception.handler.api.RestAccessDeniedHandler;
import com.Tsimur.Dubcast.exception.handler.api.RestAuthEntryPoint;
import com.Tsimur.Dubcast.model.Role;
import com.Tsimur.Dubcast.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
  @Order(1)
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**")
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(restAuthEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ApiPaths.AUTH + "/**")
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

                    // ADMIN

                    .requestMatchers(
                        ApiPaths.ADMIN + "/**",
                        ApiPaths.USERS + "/**",
                        ApiPaths.TRACK + "/**",
                        ApiPaths.SCHEDULE + "/**",
                        ApiPaths.PLAYLIST + "/**")
                    .hasAuthority(Role.ROLE_ADMIN.name())
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers(ApiPaths.PROFILE + "/**")
                    .hasAnyAuthority(Role.ROLE_USER.name(), Role.ROLE_ADMIN.name())
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/**")
        .csrf(csrf -> csrf.ignoringRequestMatchers("/radio-ws/**"))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/users/**",
                        "/error",
                        "/404",
                        "/500",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/radio-ws/**",
                        "/reel-radio-poc",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/profile/**")
                    .hasAnyRole("USER", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .permitAll())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
