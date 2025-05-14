// Arquivo: backend/src/test/java/com/backend/tessera/controller/RegisterControllerTests.java
package com.backend.tessera.controller;

import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.model.User; // Importar User
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException; // Importar para mock de sendHtmlMessage
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegisterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired // Necessário para verificar se o usuário existe antes de testar duplicação
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() throws MessagingException {
        userRepository.deleteAll(); // Limpa para garantir que os testes de "já existe" funcionem

        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendHtmlMessage(anyString(), anyString(), anyString());
    }

    @Test
    void testRegisterUser_Success_Aluno() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Aluno Teste",
                "novoalunoteste",
                "novoalunoteste@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("ALUNO")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                // Verifique a mensagem EXATA que seu RegisterController está retornando.
                // O log anterior indicava que era a mensagem mais curta.
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso! Um e-mail de confirmação foi enviado. Sua conta também será analisada pelos administradores."));
    }

    @Test
    void testRegisterUser_Success_Professor() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Professor Teste",
                "novoproftest",
                "novoproftest@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("PROFESSOR")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                 // Verifique a mensagem EXATA que seu RegisterController está retornando.
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso! Um e-mail de confirmação foi enviado. Sua conta também será analisada pelos administradores."));
    }

    @Test
    void testRegisterUser_Error_UsernameExists() throws Exception {
        // Criar um usuário para simular a existência
        User existingUser = new User("Existente", "userexists", "exists@example.com", "password", "Inst", com.backend.tessera.model.Role.ALUNO);
        userRepository.save(existingUser);

        SignupRequest signupRequest = new SignupRequest(
                "Outro Nome",
                "userexists", // Username que já existe
                "outroemail@example.com",
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
        // Criar um usuário para simular a existência do e-mail
         User existingUser = new User("Existente Email", "useremail", "emailexists@example.com", "password", "Inst", com.backend.tessera.model.Role.ALUNO);
        userRepository.save(existingUser);
        
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Email Existente",
                "alunoemailnovo",
                "emailexists@example.com", // Email que já existe
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