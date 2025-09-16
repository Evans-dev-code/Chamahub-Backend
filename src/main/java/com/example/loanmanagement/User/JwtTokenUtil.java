package com.example.loanmanagement.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private static final String SECRET_KEY = "this_is_a_very_long_secret_key_1234567890!"; // Min 32 chars
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    // ✅ Generate token based on UserEntity
    public String generateToken(UserEntity user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    // ✅ Extract username
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // ✅ Extract claims
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        Object userIdObj = getClaims(token).get("userId");

        if (userIdObj == null) {
            return null;
        }

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (userIdObj instanceof Double) {
            return ((Double) userIdObj).longValue();
        } else {
            // fallback - try to parse string representation
            try {
                return Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }


}
