package com.backend.tessera.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para centralizar a criação de loggers
 */
public class LoggerConfig {
    
    /**
     * Cria um logger para a classe especificada
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}