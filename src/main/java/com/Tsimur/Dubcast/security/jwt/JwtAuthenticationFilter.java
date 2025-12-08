package com.Tsimur.Dubcast.security.jwt;

import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String path = request.getServletPath();

        // login / register и т.п. не фильтруем
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // нет заголовка или не Bearer -> просто дальше
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String userEmail = jwtService.extractEmail(jwt);

            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(); // если нет юзера – тоже пусть полетит 401/403 потом

                if (jwtService.isValid(jwt, user)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userEmail,
                                    null,
                                    List.of(new SimpleGrantedAuthority(user.getRole().name()))
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            // любой битый / слишком короткий / с плохой подписью токен
            // -> просто не аутентифицируем пользователя и идём дальше
            // (потом сработает стандартный 401/403, но НЕ 500)
            filterChain.doFilter(request, response);
        }
    }

}

