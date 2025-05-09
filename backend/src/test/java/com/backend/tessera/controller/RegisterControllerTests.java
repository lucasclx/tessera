package com.backend.tessera.controller;

import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.model.Role;
import com.backend.tessera.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Garante que os testes sejam revertidos e não afetem o banco de dados permanentemente
public class RegisterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Limpar dados específicos se necessário, ou confiar no @Transactional
        // Ex: userRepository.deleteAll(); (Cuidado se DataInitializer rodar sempre)
    }

    @Test
    void testRegisterUser_Success_Aluno() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Aluno",
                "novoaluno",
                "novoaluno@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("ALUNO")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso! Sua conta será analisada pelos administradores."));
    }

    @Test
    void testRegisterUser_Success_Professor() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Professor",
                "novoprof",
                "novoprof@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("PROFESSOR")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso! Sua conta será analisada pelos administradores."));
    }

    @Test
    void testRegisterUser_Error_UsernameExists() throws Exception {
        // Primeiro, crie um usuário para garantir que o username já exista
        // O DataInitializer pode já ter criado "aluno1"
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Existente",
                "aluno1", // Username que já existe devido ao DataInitializer
                "alunoexistente@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("ALUNO")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro: Nome de usuário já está em uso!"));
    }

    @Test
    void testRegisterUser_Error_EmailExists() throws Exception {
        // O DataInitializer pode já ter criado "aluno@sistema.edu"
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Email Existente",
                "alunoemailnovo",
                "aluno@sistema.edu", // Email que já existe
                "password123",
                "Instituicao Teste",
                Set.of("ALUNO")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro: Email já está em uso!"));
    }
    
    @Test
    void testRegisterUser_Error_InvalidRole() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Usuario Role Invalida",
                "roleinvalidauser",
                "roleinvalida@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("INVALID_ROLE") // Role que não existe
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro: Perfil (Role) 'INVALID_ROLE' inválido."));
    }
}