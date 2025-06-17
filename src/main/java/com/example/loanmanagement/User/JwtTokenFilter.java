package com.example.loanmanagement.User;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

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
            System.out.println("🔐 JWT token detected: " + token);

            try {
                String username = jwtTokenUtil.extractUsername(token);
                String role = jwtTokenUtil.extractRole(token); // e.g., "USER" or "ADMIN"

                System.out.println("👤 Extracted Username: " + username);
                System.out.println("🔐 Extracted Role: " + role);

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                System.out.println("✅ Granted Authority: " + authority.getAuthority());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ Authentication set in SecurityContext");

            } catch (ExpiredJwtException e) {
                System.out.println("⚠️ JWT token has expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token has expired");
                return;
            } catch (MalformedJwtException e) {
                System.out.println("⚠️ Malformed JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Malformed JWT token");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                System.out.println("⚠️ Invalid JWT token: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }
        } else {
            System.out.println("❌ No Authorization header or wrong format");
        }

        filterChain.doFilter(request, response); // continue filter chain
    }
}
