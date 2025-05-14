// Arquivo: backend/src/test/java/com/backend/tessera/TesseraApplicationTests.java
package com.backend.tessera;

import com.backend.tessera.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class TesseraApplicationTests {

    @MockBean // Adicionar mock para EmailService aqui também para garantir que o contexto carregue
    private EmailService emailService; // se DataInitializer ou outros componentes o usarem na inicialização

    @Test
    void contextLoads() {
        // Este teste passa se o ApplicationContext do Spring Boot for carregado com sucesso
    }
}