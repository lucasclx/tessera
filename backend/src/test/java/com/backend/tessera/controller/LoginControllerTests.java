// Arquivo: backend/src/test/java/com/backend/tessera/controller/LoginControllerTests.java
package com.backend.tessera.controller;

import com.backend.tessera.dto.AuthRequest;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.EmailService; // Importar
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException; // Importar
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Importar
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString; // Importar
import static org.mockito.Mockito.doNothing; // Importar
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

    @MockBean // Mockar o EmailService
    private EmailService emailService;

    private User adminUser, professorUser, alunoUser, pendingUser, disabledUser;

    @BeforeEach
    void setUp() throws MessagingException { // Adicionar throws MessagingException
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendHtmlMessage(anyString(), anyString(), anyString());

        // Limpar e recriar usuários de teste para garantir um estado conhecido.
        // Isso é importante se o DataInitializer não for executado ou se for inconsistente para testes.
        userRepository.deleteAll(); // Limpa o banco antes de cada teste

        adminUser = new User("Admin Test", "admin", "admin.test@example.com", passwordEncoder.encode("password"), "Test Inst", Role.ADMIN);
        adminUser.setStatus(AccountStatus.ATIVO);
        adminUser.setEnabled(true);
        adminUser.setEmailVerified(true); // Admins podem ser verificados por padrão
        userRepository.save(adminUser);

        professorUser = new User("Professor Test", "professor1", "professor.test@example.com", passwordEncoder.encode("password"), "Test Inst", Role.PROFESSOR);
        professorUser.setStatus(AccountStatus.ATIVO);
        professorUser.setEnabled(true);
        professorUser.setEmailVerified(true); // Professores podem ser verificados por padrão
        userRepository.save(professorUser);
        
        alunoUser = new User("Aluno Test", "aluno1", "aluno.test@example.com", passwordEncoder.encode("password"), "Test Inst", Role.ALUNO);
        alunoUser.setStatus(AccountStatus.ATIVO);
        alunoUser.setEnabled(true);
        alunoUser.setEmailVerified(false); // Aluno para testar status de email não verificado
        userRepository.save(alunoUser);

        pendingUser = new User("Pendente Test", "pendente1", "pendente.test@example.com", passwordEncoder.encode("password"), "Test Inst", Role.ALUNO);
        pendingUser.setStatus(AccountStatus.PENDENTE);
        pendingUser.setEnabled(false); // Usuários pendentes estão desabilitados
        pendingUser.setEmailVerified(false);
        userRepository.save(pendingUser);

        disabledUser = new User("Disabled Test", "disableduser", "disabled.test@example.com", passwordEncoder.encode("password"), "Test Inst", Role.ALUNO);
        disabledUser.setStatus(AccountStatus.INATIVO); // Explicitamente INATIVO
        disabledUser.setEnabled(false); // E desabilitado
        disabledUser.setEmailVerified(true);
        userRepository.save(disabledUser);
    }

    @Test
    void testAuthenticateUser_Success_Admin() throws Exception {
        AuthRequest authRequest = new AuthRequest("admin", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }
    
    @Test
    void testAuthenticateUser_Success_Professor() throws Exception {
        AuthRequest authRequest = new AuthRequest("professor1", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("professor1"))
                .andExpect(jsonPath("$.roles[0]").value("PROFESSOR"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }


    @Test
    void testAuthenticateUser_Error_InvalidCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest("admin", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()) // 401 para credenciais erradas
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }
    
    @Test
    void testAuthenticateUser_Error_UserNotFound() throws Exception {
        AuthRequest authRequest = new AuthRequest("nonexistentuser", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()) // Ainda 401, mas a mensagem interna pode ser diferente
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos")); // Mensagem genérica para não revelar existência do usuário
    }


    @Test
    void testAuthenticateUser_Error_PendingApproval() throws Exception {
        AuthRequest authRequest = new AuthRequest("pendente1", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
    }
    
    @Test
    void testAuthenticateUser_Error_AccountDisabled() throws Exception {
        AuthRequest authRequest = new AuthRequest("disableduser", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden()) // Ou Unauthorized dependendo da implementação
                .andExpect(jsonPath("$.message").value("Sua conta foi desativada.")); // Mensagem do CustomAuthenticationProvider
    }


    @Test
    void testCheckApprovalStatus_ApprovedAndActive() throws Exception {
        // alunoUser é ATIVO, enabled=true, emailVerified=false
        mockMvc.perform(get("/api/auth/check-approval/aluno1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("APPROVED_EMAIL_NOT_VERIFIED")); // CORRIGIDO
    }

    @Test
    void testCheckApprovalStatus_Pending() throws Exception {
         // pendingUser é PENDENTE, enabled=false, emailVerified=false
        mockMvc.perform(get("/api/auth/check-approval/pendente1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("PENDING_APPROVAL_EMAIL_NOT_VERIFIED")); // CORRIGIDO
    }
    
    @Test
    void testCheckApprovalStatus_Disabled() throws Exception {
        // disabledUser é INATIVO, enabled=false
        mockMvc.perform(get("/api/auth/check-approval/disableduser"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("ACCOUNT_DISABLED"));
    }

    @Test
    void testCheckApprovalStatus_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/auth/check-approval/nouser"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }
}