package com.backend.tessera.model;

/**
 * Define os possíveis estados de uma conta de usuário no sistema
 */
public enum AccountStatus {
    PENDENTE,   // Aguardando aprovação do administrador
    ATIVO,      // Conta aprovada e ativa
    INATIVO     // Conta desativada temporariamente
}