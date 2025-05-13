package com.backend.tessera.controller;

import com.backend.tessera.dto.AuthRequest;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LoginControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Presume-se que DataInitializer (versão idempotente) garante que estes usuários existem
    // admin/admin123 (ADMIN, ATIVO)
    // professor1/senha123 (PROFESSOR, ATIVO)
    // aluno1/senha123 (ALUNO, ATIVO)
    // pendente1/senha123 (PROFESSOR, PENDENTE)

    @Test
    void testAuthenticateUser_Success_Admin() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0 Test")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }
    
    @Test
    void testAuthenticateUser_Success_Professor() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("professor1");
        authRequest.setPassword("senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0 Test")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("professor1"))
                .andExpect(jsonPath("$.roles[0]").value("PROFESSOR"));
    }

    @Test
    void testAuthenticateUser_Error_InvalidCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0 Test")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()) // Modificado no LoginController para retornar 401 com MessageResponse
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }

    @Test
    void testAuthenticateUser_Error_UserNotFound() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistentuser");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0 Test")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()) // Modificado no LoginController
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }
    
    @Test
    void testAuthenticateUser_Error_PendingApproval() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        // "pendente1" é criado pelo DataInitializer (idempotente) como PENDENTE
        authRequest.setUsername("pendente1"); 
        authRequest.setPassword("senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0 Test")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                // MODIFICADO AQUI para corresponder à mensagem do LoginController:
                .andExpect(jsonPath("$.message").value("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
    }

    @Test
    void testAuthenticateUser_Error_AccountDisabled() throws Exception {
        // Criar um usuário INATIVO para este teste, DataInitializer não cria um INATIVO por padrão
        User disabledUser = new User("Disabled User Test", "disabledtestuser", "disabledtest@example.com", passwordEncoder.encode("password"), "Institution", Role.ALUNO);
        disabledUser.setStatus(AccountStatus.INATIVO);
        disabledUser.setEnabled(false); 
        userRepository.save(disabledUser);
        
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("disabledtestuser");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "Mozilla/5.0 Test")
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Conta desativada. Entre em contato com o administrador."));
    }


    @Test
    void testCheckApprovalStatus_ApprovedAndActive() throws Exception {
        // "aluno1" deve ser ATIVO e enabled=true pelo DataInitializer idempotente
        mockMvc.perform(get("/api/auth/check-approval/aluno1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("APPROVED"));
    }

    @Test
    void testCheckApprovalStatus_Pending() throws Exception {
         // "pendente1" deve ser PENDENTE pelo DataInitializer idempotente
        mockMvc.perform(get("/api/auth/check-approval/pendente1"))
                .andExpect(status().isForbidden()) // LoginController.checkApprovalStatus retorna 403 para PENDENTE
                .andExpect(jsonPath("$.message").value("PENDING_APPROVAL"));
    }
    
    @Test
    void testCheckApprovalStatus_Disabled() throws Exception {
        // Criar um usuário INATIVO para este teste
        User disabledUserForCheck = new User("Check Disabled User", "checkdisableduser", "checkdisabled@example.com", passwordEncoder.encode("password"), "Institution", Role.ALUNO);
        disabledUserForCheck.setStatus(AccountStatus.INATIVO); 
        disabledUserForCheck.setRole(Role.ALUNO); 
        disabledUserForCheck.setEnabled(false); 
        userRepository.save(disabledUserForCheck);

        mockMvc.perform(get("/api/auth/check-approval/checkdisableduser"))
                .andExpect(status().isForbidden()) // LoginController.checkApprovalStatus retorna 403 para INATIVO
                .andExpect(jsonPath("$.message").value("ACCOUNT_DISABLED"));
    }


    @Test
    void testCheckApprovalStatus_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/auth/check-approval/nonexistentuser"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }
}