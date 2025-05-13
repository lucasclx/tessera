package com.backend.tessera.controller;

import com.backend.tessera.dto.SignupRequest;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User; // <<< IMPORTAÇÃO ADICIONADA AQUI
import com.backend.tessera.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // Import para PasswordEncoder se usado em setup futuro
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
    
    @Autowired // Adicionado para consistência, embora não usado diretamente neste setup simples
    private PasswordEncoder passwordEncoder;

    // Senha que atende aos critérios de @StrongPassword
    private final String STRONG_PASSWORD = "ValidPass123!";

    @BeforeEach
    void setUp() {
        // DataInitializer deve garantir que usuários como 'aluno1' e 'admin' existam.
        // Para testes de username/email existente, podemos contar com o DataInitializer
        // ou criar usuários específicos se quisermos isolamento total,
        // mas @Transactional deve reverter as criações feitas aqui.
    }

    @Test
    void testRegisterUser_Success_Aluno() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Aluno Teste",
                "novoalunoteste",
                "novoaluno.teste@example.com",
                STRONG_PASSWORD, 
                "Instituicao Teste Nova",
                Set.of("ALUNO")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Usuário registrado com sucesso!")));
    }

    @Test
    void testRegisterUser_Success_Professor() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Professor Teste",
                "novoproftest",
                "novoprof.teste@example.com",
                STRONG_PASSWORD, 
                "Instituicao Teste Nova",
                Set.of("PROFESSOR")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Usuário registrado com sucesso!")));
    }

    @Test
    void testRegisterUser_Error_UsernameExists() throws Exception {
        // DataInitializer cria 'aluno1'. Vamos tentar registrar 'aluno1' novamente.
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Username Repetido",
                "aluno1", // Username que já deve existir
                "alunorepetidouser@example.com",
                STRONG_PASSWORD,
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
         // DataInitializer cria 'aluno@sistema.edu'.
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Email Repetido",
                "alunoemailnovo2",
                "aluno@sistema.edu", // Email que já deve existir
                STRONG_PASSWORD,
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
                "Usuario Role Invalida Teste",
                "roleinvalidauser.test",
                "roleinvalida.test@example.com",
                STRONG_PASSWORD, 
                "Instituicao Teste",
                Set.of("INVALID_ROLE_TEST") 
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                // A mensagem exata pode variar um pouco dependendo da implementação do tratamento de erro,
                // mas deve indicar que o perfil é inválido.
                .andExpect(jsonPath("$.message").value("Erro: Perfil (Role) 'INVALID_ROLE_TEST' inválido."));
    }
}