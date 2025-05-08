// Arquivo: src/main/java/com/backend/tessera/security/JwtUtil.java
package com.backend.tessera.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Esta chave é específica para algoritmos HMAC-SHA (HS256, HS384, HS512)
        this.key = Keys.hmacShaKeyFor(secretString.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            // Correção: modificando para trabalhar com SecretKey em vez de Key
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            System.out.println("Erro ao extrair claims do token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);
        
        System.out.println("Gerando token para usuário: " + userDetails.getUsername());
        System.out.println("Roles: " + roles);
        
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        
        System.out.println("Token válido de " + now + " até " + expiryDate);
        
        try {
            // Atualizado para a sintaxe do JJWT 0.12.x
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            System.out.println("Erro ao criar token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
            System.out.println("Validando token para " + username + ": " + isValid);
            return isValid;
        } catch (Exception e) {
            System.out.println("Erro na validação do token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}