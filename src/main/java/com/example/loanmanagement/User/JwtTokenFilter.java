package com.example.loanmanagement.User;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    private final JwtTokenUtil jwtTokenUtil;

    public JwtTokenFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String tokenHeader = request.getHeader("Authorization");

        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7); // strip "Bearer "
            logger.info("🔐 JWT token detected: {}", token);

            try {
                String username = jwtTokenUtil.extractUsername(token);
                String role = jwtTokenUtil.extractRole(token);
                Long userId = jwtTokenUtil.extractUserId(token); // NEW: extract user ID

                if (username == null || role == null || userId == null) {
                    logger.warn("⚠️ Missing JWT claims (username, role or userId)");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid JWT token claims");
                    return;
                }

                logger.info("👤 Extracted Username: {}", username);
                logger.info("🆔 Extracted User ID: {}", userId);
                logger.info("🔐 Extracted Role: {}", role);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                logger.info("✅ Granted Authority: {}", authority.getAuthority());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

                // Attach userId to request attribute for controllers to access
                request.setAttribute("userId", userId);
                logger.info("✅ userId set in request attributes: {}", request.getAttribute("userId"));
                logger.info("JWT Filter applied to request path: {}", request.getRequestURI());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("✅ Authentication set in SecurityContext");

            } catch (ExpiredJwtException e) {
                logger.warn("⚠️ JWT token has expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token has expired");
                return;
            } catch (MalformedJwtException e) {
                logger.warn("⚠️ Malformed JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Malformed JWT token");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                logger.warn("⚠️ Invalid JWT token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }
        } else {
            logger.info("❌ No Authorization header or wrong format");
            // Optional: Clear context if no valid token
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response); // continue filter chain
    }
}
