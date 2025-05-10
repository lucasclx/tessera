package com.backend.tessera.auth.controller; // Pacote atualizado

import com.backend.tessera.auth.dto.AuthRequest; // Atualizado
import com.backend.tessera.auth.entity.AccountStatus; // Atualizado
import com.backend.tessera.auth.entity.Role; // Atualizado
import com.backend.tessera.auth.entity.User; // Atualizado
import com.backend.tessera.auth.repository.UserRepository; // Atualizado
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

    // Usuários criados pelo AuthDataInitializer (que deve ser executado antes dos testes)
    // ou garantidos pelo @BeforeEach
    // admin/admin123 (ADMIN, ATIVO, ENABLED)
    // professor1/senha123 (PROFESSOR, ATIVO, ENABLED)
    // aluno1/senha123 (ALUNO, ATIVO, ENABLED)
    // pendente1/senha123 (PROFESSOR, PENDENTE, !ENABLED)

    @BeforeEach
    void ensureBaseUsersExist() {
        // Garante que os usuários base do AuthDataInitializer existam para os testes
        // Se o AuthDataInitializer não rodar automaticamente ou for limpo, recrie-os aqui
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("Admin User", "admin", "admin@test.com", passwordEncoder.encode("admin123"), "Test Inst", Role.ADMIN, AccountStatus.ATIVO, true);
            userRepository.save(admin);
        }
        if (userRepository.findByUsername("professor1").isEmpty()) {
            User prof = new User("Prof User", "professor1", "prof1@test.com", passwordEncoder.encode("senha123"), "Test Inst", Role.PROFESSOR, AccountStatus.ATIVO, true);
            userRepository.save(prof);
        }
        if (userRepository.findByUsername("aluno1").isEmpty()) {
            User aluno = new User("Aluno User", "aluno1", "aluno1@test.com", passwordEncoder.encode("senha123"), "Test Inst", Role.ALUNO, AccountStatus.ATIVO, true);
            userRepository.save(aluno);
        }
        if (userRepository.findByUsername("pendente1").isEmpty()) {
            User pendente = new User("Pendente User", "pendente1", "pendente1@test.com", passwordEncoder.encode("senha123"), "Test Inst", Role.PROFESSOR, AccountStatus.PENDENTE, false);
            userRepository.save(pendente);
        }
        userRepository.flush(); // Garante que as transações sejam aplicadas antes dos testes
    }


    @Test
    void testAuthenticateUser_Success_Admin() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
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
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }

    @Test
    void testAuthenticateUser_Error_UserNotFound() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("nonexistentuser");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
    }

    @Test
    void testAuthenticateUser_Error_PendingApproval() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("pendente1"); // Usuário garantido pelo ensureBaseUsersExist
        authRequest.setPassword("senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Sua conta está aguardando aprovação do administrador. Tente novamente mais tarde."));
    }

    @Test
    void testAuthenticateUser_Error_AccountDisabled() throws Exception {
        // Cria um usuário INATIVO especificamente para este teste
        User disabledUser = new User("Disabled User", "disabledtestuser", "disabledtest@example.com", passwordEncoder.encode("password"), "Institution", Role.ALUNO, AccountStatus.INATIVO, false);
        userRepository.save(disabledUser);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("disabledtestuser");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                // A mensagem exata pode depender da implementação do CustomAuthenticationProvider ou UserDetailsServiceImpl
                // Se a verificação de status INATIVO vier primeiro, será esta mensagem.
                // Se a verificação de !user.isEnabled() (que considera status e o campo enabled) vier primeiro, a mensagem pode ser "Conta desabilitada..."
                .andExpect(jsonPath("$.message").value("Conta desativada. Entre em contato com o administrador."));
    }
    
    @Test
    void testAuthenticateUser_Error_AccountEnabledFalseButActiveStatus() throws Exception {
        // Cenário onde status é ATIVO mas campo 'enabled' é false
        User enabledFalseUser = new User("Enabled False User", "enabledfalseuser", "enabledfalse@example.com", passwordEncoder.encode("password"), "Institution", Role.ALUNO, AccountStatus.ATIVO, false);
        userRepository.save(enabledFalseUser);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("enabledfalseuser");
        authRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden())
                // A mensagem exata pode depender da ordem das verificações no CustomAuthenticationProvider/UserDetailsServiceImpl
                // Se User.isEnabled() (que verifica status ATIVO E campo enabled) for a principal barreira, será DisabledException
                // A mensagem "Conta desabilitada ou não ativa..." é uma boa candidata se o User.isEnabled() for a chave.
                .andExpect(jsonPath("$.message").value("Conta desabilitada ou não ativa. Entre em contato com o administrador."));
    }

    @Test
    void testCheckApprovalStatus_ApprovedAndActive() throws Exception {
        // "aluno1" é garantido como ATIVO e enabled=true pelo ensureBaseUsersExist
        mockMvc.perform(get("/api/auth/check-approval/aluno1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("APPROVED"));
    }

    @Test
    void testCheckApprovalStatus_Pending() throws Exception {
        // "pendente1" é garantido como PENDENTE e enabled=false pelo ensureBaseUsersExist
        mockMvc.perform(get("/api/auth/check-approval/pendente1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("PENDING_APPROVAL"));
    }

    @Test
    void testCheckApprovalStatus_Disabled() throws Exception {
        // Cria um usuário INATIVO especificamente para este teste
        User disabledUserForCheck = new User("Check Disabled User", "checkdisableduser", "checkdisabled@example.com", passwordEncoder.encode("password"), "Institution", Role.ALUNO, AccountStatus.INATIVO, false);
        userRepository.save(disabledUserForCheck);

        mockMvc.perform(get("/api/auth/check-approval/checkdisableduser"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("ACCOUNT_DISABLED"));
    }

    @Test
    void testCheckApprovalStatus_UserNotFound() throws Exception {
        mockMvc.perform(get("/api/auth/check-approval/nonexistentuserforcheck"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado"));
    }
}
