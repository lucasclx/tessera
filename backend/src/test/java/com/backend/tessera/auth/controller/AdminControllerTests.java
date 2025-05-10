package com.backend.tessera.auth.controller; // Pacote atualizado

import com.backend.tessera.auth.dto.UserApprovalRequest; // Atualizado
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is; // Adicionado para enabled
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse; // Adicionado para isEnabled
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUserPending;
    private User testUserActive;

    @BeforeEach
    void setUp() {
        // Limpar usuários de teste para evitar colisões entre testes, se necessário
        // userRepository.deleteAll(); // Cuidado se o DataInitializer for essencial para outros testes
        System.out.println("DEBUG [AdminControllerTests.setUp]: Iniciando setUp...");

        // Usuário PENDENTE para testes de aprovação/rejeição
        testUserPending = new User("Pending User Test", "pendingtest", "pendingtest@example.com", passwordEncoder.encode("password"), "Test Inst", Role.ALUNO, AccountStatus.PENDENTE, false);
        userRepository.save(testUserPending);

        // Usuário ATIVO para testes de desativação/status
        testUserActive = new User("Active User Test", "activetest", "activetest@example.com", passwordEncoder.encode("password"), "Test Inst Active", Role.PROFESSOR, AccountStatus.ATIVO, true);
        userRepository.save(testUserActive);

        userRepository.flush();
        System.out.println("DEBUG [AdminControllerTests.setUp]: Criado testUserPending ID: " + testUserPending.getId() + ", Status: " + testUserPending.getStatus());
        System.out.println("DEBUG [AdminControllerTests.setUp]: Criado testUserActive ID: " + testUserActive.getId() + ", Status: " + testUserActive.getStatus());

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_AsAdmin() throws Exception {
        long expectedUserCount = userRepository.count();
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize((int)expectedUserCount)))
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ALUNO"})
    void testGetAllUsers_AsNonAdmin_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetPendingUsers_AsAdmin() throws Exception {
        // Usuário do AuthDataInitializer
        userRepository.findByUsername("pendente1")
            .orElseGet(() -> userRepository.save(new User("Pendente Initializer", "pendente1", "pendente1@example.com", passwordEncoder.encode("pass"), "Inst Init", Role.ALUNO, AccountStatus.PENDENTE, false)));


        mockMvc.perform(get("/api/admin/users/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'pendingtest')].username").value("pendingtest"))
                .andExpect(jsonPath("$[?(@.username == 'pendente1')].username").value("pendente1"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_ApproveUser_AsAdmin() throws Exception {
        UserApprovalRequest approvalRequest = new UserApprovalRequest(true, "ALUNO", "Approved by admin test");

        mockMvc.perform(put("/api/admin/users/" + testUserPending.getId() + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUserPending.getUsername()))
                .andExpect(jsonPath("$.approved").value(true)) // 'approved' é true se status for ATIVO
                .andExpect(jsonPath("$.enabled").value(true))   // 'enabled' é true
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.adminComments").value("Approved by admin test"));

        User updatedUser = userRepository.findById(testUserPending.getId()).orElseThrow();
        assertEquals(AccountStatus.ATIVO, updatedUser.getStatus());
        assertTrue(updatedUser.isEnabledField()); // Verifica o campo 'enabled'
        assertEquals(Role.ALUNO, updatedUser.getRole());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_RejectUser_AsAdmin() throws Exception {
        UserApprovalRequest approvalRequest = new UserApprovalRequest(false, null, "Rejected by admin test");

        mockMvc.perform(put("/api/admin/users/" + testUserPending.getId() + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUserPending.getUsername()))
                .andExpect(jsonPath("$.approved").value(false)) // 'approved' é false se status não for ATIVO
                .andExpect(jsonPath("$.enabled").value(false))  // 'enabled' é false
                .andExpect(jsonPath("$.adminComments").value("Rejected by admin test"));

        User updatedUser = userRepository.findById(testUserPending.getId()).orElseThrow();
        // Ao rejeitar, o status pode ser PENDENTE ou INATIVO dependendo da implementação do serviço.
        // A implementação atual do UserService mantém PENDENTE e enabled=false.
        assertEquals(AccountStatus.PENDENTE, updatedUser.getStatus());
        assertFalse(updatedUser.isEnabledField());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserStatus_DisableUser_AsAdmin() throws Exception {
        // testUserActive já está ATIVO e enabled=true pelo setUp
        mockMvc.perform(put("/api/admin/users/" + testUserActive.getId() + "/status")
                        .param("enabled", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false)); // Verifica o campo 'enabled' na resposta

        User updatedUser = userRepository.findById(testUserActive.getId()).orElseThrow();
        assertEquals(AccountStatus.INATIVO, updatedUser.getStatus());
        assertFalse(updatedUser.isEnabledField()); // Verifica o campo 'enabled' na entidade
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserStatus_EnableUser_AsAdmin() throws Exception {
        // Primeiro desabilita o usuário testUserActive
        testUserActive.setEnabled(false);
        testUserActive.setStatus(AccountStatus.INATIVO);
        userRepository.saveAndFlush(testUserActive);

        mockMvc.perform(put("/api/admin/users/" + testUserActive.getId() + "/status")
                        .param("enabled", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        User updatedUser = userRepository.findById(testUserActive.getId()).orElseThrow();
        assertEquals(AccountStatus.ATIVO, updatedUser.getStatus());
        assertTrue(updatedUser.isEnabledField());
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_AsAdmin() throws Exception {
        User userToDelete = new User("ToDelete", "todeleteuser", "todelete@example.com", passwordEncoder.encode("pass"), "inst", Role.ALUNO, AccountStatus.PENDENTE, false);
        userRepository.saveAndFlush(userToDelete);
        long userId = userToDelete.getId();

        mockMvc.perform(delete("/api/admin/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário deletado com sucesso"));

        assertTrue(userRepository.findById(userId).isEmpty(), "Usuário deveria ter sido deletado do DB.");
    }
}