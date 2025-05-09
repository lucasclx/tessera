package com.backend.tessera.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "professorTest", roles = {"PROFESSOR"})
    void testGetProfessorData_AsProfessor() throws Exception {
        mockMvc.perform(get("/api/dashboard/professor/data"))
                .andExpect(status().isOk())
                .andExpect(content().string("Dados confidenciais do Dashboard do Professor para: professorTest"));
    }

    @Test
    @WithMockUser(username = "alunoTest", roles = {"ALUNO"})
    void testGetAlunoData_AsAluno() throws Exception {
        mockMvc.perform(get("/api/dashboard/aluno/data"))
                .andExpect(status().isOk())
                .andExpect(content().string("Dados específicos do Dashboard do Aluno para: alunoTest"));
    }

    @Test
    @WithMockUser(username = "adminTest", roles = {"ADMIN"})
    void testGetProfessorData_AsAdmin_Forbidden() throws Exception {
        // Admin não tem acesso direto ao dashboard de professor, a menos que também tenha o perfil PROFESSOR
        // Se a regra é estritamente ROLE_PROFESSOR, então será Forbidden.
        mockMvc.perform(get("/api/dashboard/professor/data"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alunoTest", roles = {"ALUNO"})
    void testGetProfessorData_AsAluno_Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/professor/data"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "professorTest", roles = {"PROFESSOR"})
    void testGetAlunoData_AsProfessor_Forbidden() throws Exception {
        mockMvc.perform(get("/api/dashboard/aluno/data"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetProfessorData_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/professor/data"))
                .andExpect(status().isUnauthorized()); // Ou forbidden, dependendo da config de entry point
    }

    @Test
    void testGetAlunoData_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/aluno/data"))
                 .andExpect(status().isUnauthorized()); // Ou forbidden
    }
}