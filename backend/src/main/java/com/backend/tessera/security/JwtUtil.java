package com.backend.tessera.security;

import com.backend.tessera.config.LoggerConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
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
        try {
            // Se a chave já é base64, podemos decodificá-la diretamente
            byte[] keyBytes = Decoders.BASE64.decode(secretString);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.debug("JwtUtil inicializado com sucesso usando a chave em Base64");
        } catch (Exception e) {
            // Se falhar na decodificação (chave não está em base64), vamos tratar como string normal
            logger.info("Usando chave JWT em formato de string normal");
            // Criar uma chave de 256 bits para HS256 (32 bytes)
            this.key = Keys.hmacShaKeyFor(secretString.getBytes());
            logger.debug("JwtUtil inicializado");
        }
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
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.warn("Não foi possível determinar a expiração do token: {}", e.getMessage());
            return true;
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);
        
        logger.debug("Gerando token para usuário: {} com roles: {}", userDetails.getUsername(), roles);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        
        logger.debug("Token válido de {} até {}", now, expiryDate);
        
        try {
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            logger.error("Erro ao criar token: {}", e.getMessage(), e);
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