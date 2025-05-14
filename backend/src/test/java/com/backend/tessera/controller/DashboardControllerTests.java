// Arquivo: backend/src/test/java/com/backend/tessera/controller/DashboardControllerTests.java
package com.backend.tessera.controller;

import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import com.backend.tessera.service.EmailService; // Importar
import jakarta.mail.MessagingException; // Importar
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Importar
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString; // Importar
import static org.mockito.Mockito.doNothing; // Importar
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository; // Para setup, se necessário

    @MockBean // Mockar o EmailService
    private EmailService emailService;

    @BeforeEach
    void setUp() throws MessagingException { // Adicionar throws MessagingException
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
        doNothing().when(emailService).sendHtmlMessage(anyString(), anyString(), anyString());

        // Pode ser necessário criar usuários de teste aqui se o DataInitializer não for suficiente
        // ou se você quiser estados específicos para os usuários.
        // Exemplo:
        // User professor = new User("profTestDash", "password", Role.PROFESSOR);
        // professor.setEmail("profTestDash@example.com");
        // professor.setNome("Professor Teste Dash");
        // professor.setInstitution("Test Inst");
        // userRepository.save(professor);
        //
        // User aluno = new User("alunoTestDash", "password", Role.ALUNO);
        // aluno.setEmail("alunoTestDash@example.com");
        // aluno.setNome("Aluno Teste Dash");
        // aluno.setInstitution("Test Inst");
        // userRepository.save(aluno);
    }

    @Test
    @WithMockUser(username = "professor1", roles = {"PROFESSOR"})
    void testGetProfessorData_AsProfessor() throws Exception {
        mockMvc.perform(get("/api/dashboard/professor"))
                .andExpect(status().isOk())
                .andExpect(content().string("Dados do dashboard do Professor para: professor1"));
    }

    @Test
    @WithMockUser(username = "aluno1", roles = {"ALUNO"})
    void testGetProfessorData_AsAluno_Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/professor"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetProfessorData_AsAdmin_Forbidden() throws Exception {
        // Assumindo que Admin não tem acesso direto ao dashboard de professor
        mockMvc.perform(get("/api/dashboard/professor"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetProfessorData_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/professor"))
                .andExpect(status().isUnauthorized()); // Ou .isForbidden() dependendo do entry point
    }

    @Test
    @WithMockUser(username = "aluno1", roles = {"ALUNO"})
    void testGetAlunoData_AsAluno() throws Exception {
        mockMvc.perform(get("/api/dashboard/aluno"))
                .andExpect(status().isOk())
                .andExpect(content().string("Dados do dashboard do Aluno para: aluno1"));
    }

    @Test
    @WithMockUser(username = "professor1", roles = {"PROFESSOR"})
    void testGetAlunoData_AsProfessor_Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/aluno"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAlunoData_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/aluno"))
                .andExpect(status().isUnauthorized()); // Ou .isForbidden()
    }
}