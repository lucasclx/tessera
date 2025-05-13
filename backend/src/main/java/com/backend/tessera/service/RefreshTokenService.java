package com.backend.tessera.service;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.exception.ResourceNotFoundException;
import com.backend.tessera.model.RefreshToken;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.RefreshTokenRepository;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private static final Logger logger = LoggerConfig.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.refresh.expiration}")
    private int refreshTokenDurationMs;

    @Transactional
    public RefreshToken createRefreshToken(String username, String userAgent, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.forUser(username));

        RefreshToken refreshToken = RefreshToken.createToken(
                user, refreshTokenDurationMs, userAgent, ipAddress);

        logger.debug("Criando refresh token para usuário: {}", username);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired() || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            logger.warn("Refresh token expirado ou revogado: {}", token.getToken());
            throw new RuntimeException("Refresh token expirado. Por favor, faça login novamente.");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        logger.info("Refresh tokens excluídos para o usuário ID: {}", userId);
    }

    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token não encontrado!"));
        
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token revogado: {}", token);
    }

    @Scheduled(fixedRate = 86400000) // Executa a cada 24 horas
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Limpando tokens expirados...");
        refreshTokenRepository.findAll().stream()
                .filter(RefreshToken::isExpired)
                .forEach(refreshTokenRepository::delete);
    }
}