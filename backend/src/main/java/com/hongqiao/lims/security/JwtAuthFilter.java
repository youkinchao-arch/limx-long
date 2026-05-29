package com.hongqiao.lims.security;

import com.hongqiao.lims.user.User;
import com.hongqiao.lims.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7);
            try {
                io.jsonwebtoken.Claims claims = jwtService.parse(token);
                Long userId = Long.valueOf(claims.getSubject());
                Integer tokenVersion = claims.get("tv", Integer.class);
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.isActive()
                        && tokenVersion != null && tokenVersion == user.getTokenVersion()) {
                    List<SimpleGrantedAuthority> authorities = user.effectivePermissions().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // Invalid/expired token: leave the context unauthenticated.
            }
        }
        chain.doFilter(request, response);
    }
}
