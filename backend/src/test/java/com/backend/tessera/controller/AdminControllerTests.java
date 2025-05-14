// Arquivo: backend/src/test/java/com/backend/tessera/controller/AdminControllerTests.java
package com.backend.tessera.controller;

import com.backend.tessera.dto.UserApprovalRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString; // Importar
import static org.mockito.Mockito.doNothing; // Importar
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @MockBean // Mockar o EmailService
    private EmailService emailService;

    private User adminUser;
    private User professorUser;
    private User alunoUser;
    private User pendingUser;

    @BeforeEach
    void setUp() throws MessagingException { // Adicionar throws MessagingException
        // Limpar usuários de testes anteriores para evitar conflitos, se necessário
        // userRepository.deleteAll(); // Cuidado se o DataInitializer for executado por teste

        // Configurar o mock do EmailService
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendHtmlMessage(anyString(), anyString(), anyString());


        // Criar usuários para os testes (ou carregar do DataInitializer se for consistente)
        // Se DataInitializer já cria esses, pode não ser necessário criar aqui,
        // mas garanta que eles tenham os estados esperados.
        adminUser = userRepository.findByUsername("admin").orElseGet(() ->
                userRepository.save(new User("Admin User", "admin", "admin@sistema.edu", "password", "Sys", Role.ADMIN))
        );
        if(adminUser.getStatus() != AccountStatus.ATIVO) {
            adminUser.setStatus(AccountStatus.ATIVO);
            adminUser.setEnabled(true);
            adminUser = userRepository.save(adminUser);
        }


        professorUser = userRepository.findByUsername("professor1").orElseGet(() ->
                userRepository.save(new User("Professor Um", "professor1", "professor1@sistema.edu", "password", "Inst", Role.PROFESSOR))
        );
         if(professorUser.getStatus() != AccountStatus.ATIVO) {
            professorUser.setStatus(AccountStatus.ATIVO);
            professorUser.setEnabled(true);
            professorUser = userRepository.save(professorUser);
        }


        alunoUser = userRepository.findByUsername("aluno1").orElseGet(() ->
            userRepository.save(new User("Aluno Um", "aluno1", "aluno1@sistema.edu", "password", "Inst", Role.ALUNO))
        );
        if(alunoUser.getStatus() != AccountStatus.ATIVO) {
            alunoUser.setStatus(AccountStatus.ATIVO);
            alunoUser.setEnabled(true);
            alunoUser = userRepository.save(alunoUser);
        }

        pendingUser = userRepository.findByUsername("pendente1").orElseGet(() ->
            userRepository.save(new User("Pendente Um", "pendente1", "pendente1@sistema.edu", "password", "Inst", Role.ALUNO))
        );
        // Garantir que o usuário pendente esteja realmente pendente
        if(pendingUser.getStatus() != AccountStatus.PENDENTE || pendingUser.isEnabled()){
            pendingUser.setStatus(AccountStatus.PENDENTE);
            pendingUser.setEnabled(false);
            pendingUser = userRepository.save(pendingUser);
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllUsers_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").exists()); // Verifica se há pelo menos um usuário
    }

    @Test
    @WithMockUser(username = "professor1", roles = {"PROFESSOR"})
    void testGetAllUsers_AsNonAdmin_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAllUsers_Unauthenticated_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Ou .isForbidden() dependendo da config exata do entrypoint
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetPendingUsers_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        // Adicionar verificação se o usuário 'pendente1' está na lista
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_ApproveUser_AsAdmin() throws Exception {
        UserApprovalRequest request = new UserApprovalRequest();
        request.setApprove(true);
        request.setAdminComments("Usuário aprovado pelo admin.");

        mockMvc.perform(patch("/api/admin/users/{userId}/approval", pendingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.adminComments").value("Usuário aprovado pelo admin."));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserApproval_RejectUser_AsAdmin() throws Exception {
        UserApprovalRequest request = new UserApprovalRequest();
        request.setApprove(false);
        request.setAdminComments("Usuário rejeitado pelo admin.");

        mockMvc.perform(patch("/api/admin/users/{userId}/approval", pendingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO")) // Ou REJEITADO se existir esse status
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.adminComments").value("Usuário rejeitado pelo admin."));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUserStatus_DisableUser_AsAdmin() throws Exception {
        // Certifique-se que alunoUser está ativo primeiro
        alunoUser.setStatus(AccountStatus.ATIVO);
        alunoUser.setEnabled(true);
        userRepository.save(alunoUser);

        mockMvc.perform(patch("/api/admin/users/{userId}/status", alunoUser.getId())
                        .param("enable", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.status").value("INATIVO")); // Ou mantém ATIVO mas enabled=false
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteUser_AsAdmin() throws Exception {
        User userToDelete = userRepository.save(new User("Delete Me", "deleteme", "deleteme@example.com", "password", "Inst", Role.ALUNO));
        userToDelete.setStatus(AccountStatus.PENDENTE); // Para poder deletar
        userRepository.save(userToDelete);

        mockMvc.perform(delete("/api/admin/users/{userId}", userToDelete.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário deletado com sucesso."));
    }
}