package com.backend.tessera.controller;

import com.backend.tessera.dto.UserApprovalRequest;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

    @BeforeEach
    void setUp() {
        // Criar um usuário pendente para os testes
        testUserPending = new User();
        testUserPending.setNome("Pending User Test");
        testUserPending.setUsername("pendingtest");
        testUserPending.setEmail("pendingtest@example.com");
        testUserPending.setPassword(passwordEncoder.encode("password"));
        testUserPending.setInstitution("Test Inst");
        testUserPending.setRole(Role.ALUNO);
        testUserPending.setStatus(AccountStatus.PENDENTE);
        // Não precisamos mais chamar setEnabled(false) pois isso é determinado pelo status
        userRepository.save(testUserPending);
        userRepository.flush();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_AsAdmin() throws Exception {
        long expectedUserCount = userRepository.count(); 

        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(expectedUserCount));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_ApproveUser_AsAdmin() throws Exception {
        User userToApprove = userRepository.findByUsername("pendingtest").orElse(null);
        if (userToApprove == null) {
            assertTrue(false, "'pendingtest' deveria existir após o setUp para este teste."); 
        }

        UserApprovalRequest approvalRequest = new UserApprovalRequest(true, "ALUNO", "Approved by admin test");

        mockMvc.perform(put("/api/admin/users/" + userToApprove.getId() + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userToApprove.getUsername()))
                .andExpect(jsonPath("$.approved").value(true))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.adminComments").value("Approved by admin test"));

        User updatedUser = userRepository.findById(userToApprove.getId()).orElseThrow();
        assertEquals(AccountStatus.ATIVO, updatedUser.getStatus());
        assertTrue(updatedUser.isEnabled());
        assertEquals(Role.ALUNO, updatedUser.getRole());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_RejectUser_AsAdmin() throws Exception {
        User userToReject = userRepository.findByUsername("pendingtest").orElse(null);
        if (userToReject == null) {
            assertTrue(false, "'pendingtest' deveria existir após o setUp para este teste.");
        }

        UserApprovalRequest approvalRequest = new UserApprovalRequest(false, null, "Rejected by admin test");

        mockMvc.perform(put("/api/admin/users/" + userToReject.getId() + "/approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(userToReject.getUsername()))
                .andExpect(jsonPath("$.approved").value(false)) 
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.adminComments").value("Rejected by admin test"));

        User updatedUser = userRepository.findById(userToReject.getId()).orElseThrow();
        assertEquals(AccountStatus.REJEITADO, updatedUser.getStatus()); // Agora verificamos REJEITADO
        assertEquals(false, updatedUser.isEnabled());
    }

    // Os demais testes seguem o mesmo padrão de adaptação...
}