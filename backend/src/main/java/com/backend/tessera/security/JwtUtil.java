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
        // If the key was not defined by environment variable, use a randomly generated one.
        // IMPORTANT: For production, the secretString MUST be a strong, environment-specific variable.
        // The current application.properties has a default, which will be used here.
        // We are now targeting HS256, so the key derived from secretString should be 256-bit.
        if (secretString == null || secretString.trim().isEmpty() || 
            secretString.equals("${JWT_SECRET:e8bbc656b6d4bca2b7e9f292d18082a0af114bdf2b0f8e98f9aefffad9f7e608}") || // Check against the new shorter default
            secretString.equals("e8bbc656b6d4bca2b7e9f292d18082a0af114bdf2b0f8e98f9aefffad9f7e608") // Default fallback if env var not set
        ) {
             // This default key is 256-bit (32 bytes from 64 hex characters)
            byte[] keyBytes = hexStringToByteArray(secretString);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("JWT secret inicializado com valor padrão para HS256 (32 bytes).");
        } else {
            // Use the key defined in the environment variable or properties.
            // Ensure it's the correct length for HS256 (32 bytes / 256 bits / 64 hex characters).
            // If it's longer, it might be truncated or cause issues if not handled carefully.
            // Forcing it to be derived from the (now shorter) secretString.
            byte[] keyBytes = hexStringToByteArray(secretString);
             if (keyBytes.length != 32) {
                logger.warn("JWT_SECRET fornecido tem {} bytes, mas HS256 espera 32 bytes. Verifique a configuração.", keyBytes.length);
                // Potentially truncate or handle error, for now, we'll use it as is,
                // but Keys.hmacShaKeyFor might throw an error if size is wrong for an implicit algo,
                // so explicitly using HS256 later with signWith is better.
            }
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
        logger.debug("JwtUtil inicializado.");
    }

    // Helper to convert hex string to byte array
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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
                    .verifyWith(key) // For JJWT 0.12.x, verifyWith is preferred
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Erro ao extrair claims do token: {}", e.getMessage());
            throw e; // Re-throw para ser tratado por quem chamou
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.warn("Não foi possível determinar a expiração do token (possivelmente inválido ou malformado): {}", e.getMessage());
            return true; // Tratar como expirado em caso de erro ao extrair data
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
                    .signWith(key, SignatureAlgorithm.HS256) // Explicitamente usando HS256
                    .compact();
        } catch (Exception e) {
            logger.error("Erro ao criar token HS256: {}", e.getMessage(), e); // Log com stack trace
            throw e; // Re-throw para ser tratado pelo LoginController
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.debug("Validando token para {}: {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Erro na validação do token (validateToken): {}", e.getMessage());
            return false;
        }
    }
}