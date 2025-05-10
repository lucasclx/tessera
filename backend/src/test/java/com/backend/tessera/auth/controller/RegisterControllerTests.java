package com.backend.tessera.auth.controller;

import com.backend.tessera.auth.dto.SignupRequest;
import com.backend.tessera.auth.entity.Role; // Importado para clareza, embora não usado diretamente nas asserções
import com.backend.tessera.auth.repository.UserRepository;
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
    private UserRepository userRepository; // Pode ser usado para preparar dados específicos se não depender apenas do Initializer

    @BeforeEach
    void setUp() {
        // Para RegisterControllerTests, geralmente confiamos no AuthDataInitializer para usuários existentes
        // que causam conflito, e no @Transactional para limpar os usuários recém-registrados.
        // Um método como `ensureBaseUsersExist` do LoginControllerTests pode não ser necessário aqui,
        // já que estamos testando o processo de criação ou conflitos com usuários conhecidos.
        // Se fosse necessário limpar explicitamente ou criar usuários base específicos para todos os testes de registro,
        // este seria o local.
    }

    @Test
    void testRegisterUser_Success_Aluno() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Novo Aluno Teste", // Nome completo
                "novoalunoteste",    // Username único para o teste
                "novoaluno.teste@example.com", // Email único para o teste
                "password123",
                "Instituicao Teste Aluno",
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
                "Novo Professor Teste", // Nome completo
                "novoproftest",      // Username único para o teste
                "novoprof.teste@example.com", // Email único para o teste
                "password123",
                "Instituicao Teste Prof",
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
        // Assumindo que "aluno1" é criado pelo AuthDataInitializer e, portanto, já existe.
        // Se AuthDataInitializer não for executado ou for limpo, este teste pode precisar criar "aluno1" primeiro.
        // No entanto, com um Initializer idempotente, podemos assumir sua existência.
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Duplicado",
                "aluno1", // Username que já deve existir (do AuthDataInitializer)
                "alunoduplicado.email@example.com", // Email diferente para isolar o erro de username
                "password123",
                "Instituicao Teste Duplicado",
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
        // Assumindo que "aluno@sistema.edu" é o email do usuário "aluno1" criado pelo AuthDataInitializer.
        SignupRequest signupRequest = new SignupRequest(
                "Aluno Email Duplicado",
                "alunoemailnovo", // Username diferente para isolar o erro de email
                "aluno@sistema.edu", // Email que já deve existir
                "password123",
                "Instituicao Teste Email Duplicado",
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
                "roleinvalidauser",
                "roleinvalida@example.com",
                "password123",
                "Instituicao Teste Role Invalida",
                Set.of("INVALID_ROLE_TEST") // Uma role que não existe no Enum Role
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                // A mensagem exata depende da implementação do RegisterController
                .andExpect(jsonPath("$.message").value("Erro: Perfil (Role) 'INVALID_ROLE_TEST' inválido."));
    }

    @Test
    void testRegisterUser_Error_AdminRoleNotAllowed() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Tentativa Admin",
                "tentativaadmin",
                "tentativaadmin@example.com",
                "password123",
                "Instituicao Teste",
                Set.of("ADMIN") // Tentativa de registrar como ADMIN
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro: Registro como ADMIN não é permitido."));
    }

    @Test
    void testRegisterUser_Error_NoRoleSpecified() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "Usuario Sem Role",
                "semroleuser",
                "semrole@example.com",
                "password123",
                "Instituicao Teste Sem Role",
                Set.of() // Nenhum perfil especificado
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro: Perfil (Role) não especificado."));
    }
}
