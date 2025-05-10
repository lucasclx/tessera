package com.backend.tessera.auth.entity;

public enum AccountStatus {
    PENDENTE,   // Aguardando aprovação do administrador
    ATIVO,      // Conta aprovada e ativa
    INATIVO     // Conta desativada (pode ser temporário ou resultado de rejeição/banimento)
}