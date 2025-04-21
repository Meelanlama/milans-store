package com.milan.security;

import com.milan.exception.JwtTokenExpiredException;
import com.milan.model.SiteUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;


    private String buildToken(SiteUser user, Map<String, Object> claims, long expirationMillis) {
        return Jwts.builder()
                .claims().add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String generateAccessToken(SiteUser user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("roles", user.getRoles());
        claims.put("status", user.getAccountStatus().getIsAccountActive());

        // 15 minutes expressed in milliseconds.
        long expirationMillis = 1000 * 60 * 15;
        return buildToken(user, claims, expirationMillis);
    }

    public String generateRefreshToken(SiteUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("roles", user.getRoles());

        // 7 days expressed in milliseconds.
        long expirationMillis = 1000L * 60 * 60 * 24 * 7;
        return buildToken(user, claims, expirationMillis);
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey decryptKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(decryptKey(secretKey))
                    .build().parseSignedClaims(token).getPayload();
        }
        catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException("Token is Expired");
        }catch (JwtException e) {
            throw new JwtTokenExpiredException("Invalid Jwt token");
        }catch (Exception e) {
            throw e;
        }
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {

        String username = extractUsername(token);
        Boolean isExpired=isTokenExpired(token);
        if(username.equalsIgnoreCase(userDetails.getUsername()) && !isExpired)
        {
            return true;
        }
        return false;
    }

    private Boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);
        Date expiredDate = claims.getExpiration();
        return expiredDate.before(new Date());
    }

}
