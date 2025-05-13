package com.backend.tessera.security;

import com.backend.tessera.config.LoggerConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerConfig.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;
    
    private SecretKey key;

    @PostConstruct
    public void init() {
        // Se a chave não foi definida por variável de ambiente, use uma gerada automaticamente
        if (secretString == null || secretString.trim().isEmpty() || 
            secretString.equals("${JWT_SECRET}")) {
            logger.warn("JWT secret não definido! Gerando uma chave aleatória para esta sessão...");
            // Gerar uma chave aleatória para desenvolvimento - NÃO USAR EM PRODUÇÃO
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            // Use a chave definida em variável de ambiente
            this.key = Keys.hmacShaKeyFor(secretString.getBytes());
        }
        logger.debug("JwtUtil inicializado com chave secreta");
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
            // Utilizando a sintaxe do JJWT 0.12.x
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Erro ao extrair claims do token: {}", e.getMessage());
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
        
        logger.debug("Gerando token para usuário: {}", userDetails.getUsername());
        logger.debug("Roles: {}", roles);
        
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        
        logger.debug("Token válido de {} até {}", now, expiryDate);
        
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
            logger.error("Erro ao criar token: {}", e.getMessage());
            throw e;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.debug("Validando token para {}: {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Erro na validação do token: {}", e.getMessage());
            return false;
        }
    }
}