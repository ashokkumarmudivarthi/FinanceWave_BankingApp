package com.financewave.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.financewave.auth.service.BlacklistService;
import com.financewave.auth.service.SessionService;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ BYPASS ONLY PUBLIC AUTH APIs
        if (path.equals("/auth/login") ||
            path.equals("/auth/register") ||
            path.equals("/auth/refresh") ||
            path.equals("/auth/forgot-password") ||
            path.equals("/auth/reset-password") ||
            path.equals("/auth/send-otp") ||
            path.equals("/auth/resend-otp") ||
            path.equals("/auth/verify-otp")) {

            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        // ✅ Extract token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            // ❌ BLOCK BLACKLISTED TOKEN
            if (blacklistService.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // ❌ BLOCK INVALID SESSION
            if (!sessionService.isSessionValid(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            // ❌ No token → block
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ✅ SET AUTHENTICATION
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(token)) {

                String role = jwtUtil.extractRole(token);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(
                                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)
                                )
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ✅ Continue filter chain
        chain.doFilter(request, response);
    }
}