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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeEach
    void setUp() {
        System.out.println("DEBUG [AdminControllerTests.setUp]: Iniciando setUp...");
        // Este usuário é criado antes de cada teste e será revertido pelo @Transactional
        testUserPending = new User("Pending User Test", "pendingtest", "pendingtest@example.com", passwordEncoder.encode("password"), "Test Inst", Role.ALUNO);
        testUserPending.setStatus(AccountStatus.PENDENTE);
        testUserPending.setEnabled(false);
        userRepository.save(testUserPending); 
        userRepository.flush(); // Força a sincronização com o banco de dados
        System.out.println("DEBUG [AdminControllerTests.setUp]: Criado e salvo testUserPending com username: " + testUserPending.getUsername() + ", ID: " + testUserPending.getId() + ", Status: " + testUserPending.getStatus());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_AsAdmin() throws Exception {
        System.out.println("\n--- DEBUG INÍCIO: testGetAllUsers_AsAdmin ---");
        long expectedUserCount = userRepository.count(); 
        System.out.println("DEBUG [testGetAllUsers_AsAdmin]: Contagem total de usuários no repo: " + expectedUserCount);
        userRepository.findAll().forEach(u -> System.out.println("DEBUG [testGetAllUsers_AsAdmin]: Usuário no DB: ID=" + u.getId() + ", Username=" + u.getUsername() + ", Status=" + u.getStatus()));
        System.out.println("--- DEBUG FIM: testGetAllUsers_AsAdmin ---\n");

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
    void testGetAllUsers_Unauthenticated_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetPendingUsers_AsAdmin() throws Exception {
        System.out.println("\n--- DEBUG INÍCIO: testGetPendingUsers_AsAdmin ---");
        
        // Verifica o usuário 'pendente1' do DataInitializer
        User pendente1FromDb = userRepository.findByUsername("pendente1").orElse(null);
        if (pendente1FromDb == null) {
            System.err.println("DEBUG [testGetPendingUsers_AsAdmin]: 'pendente1' (do DataInitializer) NÃO FOI ENCONTRADO no DB!");
        } else {
            System.out.println("DEBUG [testGetPendingUsers_AsAdmin]: 'pendente1' (do DataInitializer) ENCONTRADO. ID: " + pendente1FromDb.getId() + ", Username: " + pendente1FromDb.getUsername() + ", Status: " + pendente1FromDb.getStatus() + ", Enabled: " + pendente1FromDb.isEnabled());
            if (pendente1FromDb.getStatus() != AccountStatus.PENDENTE) {
                System.err.println("DEBUG [testGetPendingUsers_AsAdmin]: 'pendente1' Status NÃO É PENDENTE como esperado! É: " + pendente1FromDb.getStatus());
            }
        }

        // Verifica o usuário 'pendingtest' criado no setUp deste teste
        User pendingTestFromDb = userRepository.findByUsername("pendingtest").orElse(null);
         if (pendingTestFromDb == null) {
            System.err.println("DEBUG [testGetPendingUsers_AsAdmin]: 'pendingtest' (do setUp) NÃO FOI ENCONTRADO no DB!");
        } else {
            System.out.println("DEBUG [testGetPendingUsers_AsAdmin]: 'pendingtest' (do setUp) ENCONTRADO. ID: " + pendingTestFromDb.getId() + ", Username: " + pendingTestFromDb.getUsername() + ", Status: " + pendingTestFromDb.getStatus() + ", Enabled: " + pendingTestFromDb.isEnabled());
             if (pendingTestFromDb.getStatus() != AccountStatus.PENDENTE) {
                System.err.println("DEBUG [testGetPendingUsers_AsAdmin]: 'pendingtest' Status NÃO É PENDENTE como esperado! É: " + pendingTestFromDb.getStatus());
            }
        }
        
        // Lista todos os usuários pendentes diretamente do repositório
        List<User> allPendingDirectly = userRepository.findAll().stream()
                                            .filter(u -> u.getStatus() == AccountStatus.PENDENTE)
                                            .collect(Collectors.toList());
        System.out.println("DEBUG [testGetPendingUsers_AsAdmin]: Usuários PENDENTES diretos do repo (ID | Username | Status): " +
            allPendingDirectly.stream()
                              .map(u -> u.getId() + " | " + u.getUsername() + " | " + u.getStatus())
                              .collect(Collectors.toList()));
        System.out.println("--- DEBUG FIM: testGetPendingUsers_AsAdmin ---\n");

        // Ação do teste
        mockMvc.perform(get("/api/admin/users/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'pendingtest')].username").value("pendingtest"))
                .andExpect(jsonPath("$[?(@.username == 'pendente1')].username").value("pendente1"));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_ApproveUser_AsAdmin() throws Exception {
        System.out.println("\n--- DEBUG INÍCIO: testUpdateUserApproval_ApproveUser_AsAdmin ---");
        User userToApprove = userRepository.findByUsername("pendingtest").orElse(null);
        if (userToApprove == null) {
             System.err.println("DEBUG [testUpdateUserApproval_ApproveUser_AsAdmin]: 'pendingtest' NÃO encontrado para aprovação!");
             // Falha o teste se o usuário de pré-condição não existir
             assertTrue(false, "'pendingtest' deveria existir após o setUp para este teste."); 
        } else {
            System.out.println("DEBUG [testUpdateUserApproval_ApproveUser_AsAdmin]: 'pendingtest' encontrado para aprovação. ID: " + userToApprove.getId());
        }
        System.out.println("--- DEBUG FIM: testUpdateUserApproval_ApproveUser_AsAdmin ---\n");

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
        System.out.println("\n--- DEBUG INÍCIO: testUpdateUserApproval_RejectUser_AsAdmin ---");
        User userToReject = userRepository.findByUsername("pendingtest").orElse(null);
         if (userToReject == null) {
             System.err.println("DEBUG [testUpdateUserApproval_RejectUser_AsAdmin]: 'pendingtest' NÃO encontrado para rejeição!");
             assertTrue(false, "'pendingtest' deveria existir após o setUp para este teste.");
        } else {
            System.out.println("DEBUG [testUpdateUserApproval_RejectUser_AsAdmin]: 'pendingtest' encontrado para rejeição. ID: " + userToReject.getId());
        }
        System.out.println("--- DEBUG FIM: testUpdateUserApproval_RejectUser_AsAdmin ---\n");

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
        assertEquals(AccountStatus.PENDENTE, updatedUser.getStatus()); 
        assertEquals(false, updatedUser.isEnabled());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserStatus_DisableUser_AsAdmin() throws Exception {
        System.out.println("\n--- DEBUG INÍCIO: testUpdateUserStatus_DisableUser_AsAdmin ---");
        User userToDisable = userRepository.findByUsername("pendingtest").orElse(null);
        if (userToDisable == null) {
             System.err.println("DEBUG [testUpdateUserStatus_DisableUser_AsAdmin]: 'pendingtest' NÃO encontrado para desabilitar!");
             assertTrue(false, "'pendingtest' deveria existir após o setUp para este teste.");
        } else {
             System.out.println("DEBUG [testUpdateUserStatus_DisableUser_AsAdmin]: 'pendingtest' encontrado para desabilitar. ID: " + userToDisable.getId());
        }
        
        // Primeiro, aprovamos o usuário para que ele possa ser desativado (status ATIVO)
        userToDisable.setStatus(AccountStatus.ATIVO);
        userToDisable.setEnabled(true);
        userRepository.saveAndFlush(userToDisable); // Salva e força o flush
        System.out.println("DEBUG [testUpdateUserStatus_DisableUser_AsAdmin]: 'pendingtest' atualizado para ATIVO/ENABLED antes de desabilitar. Status: " + userToDisable.getStatus() + ", Enabled: " + userToDisable.isEnabled());
        System.out.println("--- DEBUG FIM: testUpdateUserStatus_DisableUser_AsAdmin ---\n");

        mockMvc.perform(put("/api/admin/users/" + userToDisable.getId() + "/status")
                        .param("enabled", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        User updatedUser = userRepository.findById(userToDisable.getId()).orElseThrow();
        assertEquals(AccountStatus.INATIVO, updatedUser.getStatus());
        assertEquals(false, updatedUser.isEnabled());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_AsAdmin() throws Exception {
        User userToDelete = new User("ToDelete", "todeleteuser", "todelete@example.com", passwordEncoder.encode("pass"), "inst", Role.ALUNO);
        userRepository.saveAndFlush(userToDelete); // Salva e força o flush
        long userId = userToDelete.getId();
        System.out.println("DEBUG [testDeleteUser_AsAdmin]: Criado userToDelete com ID: " + userId);


        mockMvc.perform(delete("/api/admin/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário deletado com sucesso"));
        
        assertTrue(userRepository.findById(userId).isEmpty(), "Usuário deveria ter sido deletado do DB.");
    }
}