package com.backend.tessera;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Importar para transação

import java.time.LocalDateTime; // Importar LocalDateTime
import java.util.Optional;       // Importar Optional

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Garante que todas as operações no método sejam parte de uma única transação
    public void run(String... args) throws Exception {
        boolean createdAnyUser = false;
        System.out.println(">>> Iniciando DataInitializer...");

        // --- Usuário Admin ---
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setNome("Administrador");
            admin.setUsername("admin");
            admin.setEmail("admin@sistema.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setInstitution("Sistema Acadêmico");
            admin.setRole(Role.ADMIN);
            admin.setStatus(AccountStatus.ATIVO);
            admin.setEnabled(true);
            admin.setEmailVerified(true); // Definir como verificado
            admin.setEmailVerifiedAt(LocalDateTime.now()); // Definir data de verificação
            admin.setAdminComments("Usuário administrador padrão criado.");
            userRepository.save(admin);
            System.out.println(">>> Usuário Administrador criado: admin/admin123");
            createdAnyUser = true;
        } else {
            User existingAdmin = adminOpt.get();
            boolean updatedAdmin = false;
            if (!existingAdmin.isEnabled()) {
                existingAdmin.setEnabled(true);
                existingAdmin.setStatus(AccountStatus.ATIVO); // Garante status ATIVO se estiver habilitando
                updatedAdmin = true;
            }
            if (!existingAdmin.isEmailVerified()) {
                existingAdmin.setEmailVerified(true);
                existingAdmin.setEmailVerifiedAt(LocalDateTime.now()); // Opcional: definir data de verificação
                updatedAdmin = true;
            }
            if (updatedAdmin) {
                userRepository.save(existingAdmin);
                System.out.println(">>> Usuário 'admin' existente atualizado para enabled=true e emailVerified=true.");
            } else {
                System.out.println(">>> Usuário 'admin' já existe e está configurado corretamente.");
            }
        }

        // --- Usuário Professor de Teste ---
        if (userRepository.findByUsername("professor1").isEmpty()) {
            User professor = new User();
            professor.setNome("Professor Exemplo");
            professor.setUsername("professor1");
            professor.setEmail("professor@sistema.edu");
            professor.setPassword(passwordEncoder.encode("senha123"));
            professor.setInstitution("Sistema Acadêmico");
            professor.setRole(Role.PROFESSOR);
            professor.setStatus(AccountStatus.ATIVO);
            professor.setEnabled(true);
            professor.setEmailVerified(true); // Definir como verificado
            professor.setEmailVerifiedAt(LocalDateTime.now()); // Definir data de verificação
            professor.setAdminComments("Usuário professor de teste, aprovado e verificado via Initializer.");
            userRepository.save(professor);
            System.out.println(">>> Usuário Professor de teste criado: professor1/senha123");
            createdAnyUser = true;
        } else {
            // Opcional: Poderia adicionar lógica similar à do admin para verificar/atualizar professor1 se necessário
             System.out.println(">>> Usuário 'professor1' já existe.");
        }

        // --- Usuário Aluno de Teste ---
        if (userRepository.findByUsername("aluno1").isEmpty()) {
            User aluno = new User();
            aluno.setNome("Aluno Exemplo");
            aluno.setUsername("aluno1");
            aluno.setEmail("aluno@sistema.edu");
            aluno.setPassword(passwordEncoder.encode("senha123"));
            aluno.setInstitution("Sistema Acadêmico");
            aluno.setRole(Role.ALUNO);
            aluno.setStatus(AccountStatus.ATIVO);
            aluno.setEnabled(true);
            aluno.setEmailVerified(true); // Definir como verificado
            aluno.setEmailVerifiedAt(LocalDateTime.now()); // Definir data de verificação
            aluno.setAdminComments("Usuário aluno de teste, aprovado e verificado via Initializer.");
            userRepository.save(aluno);
            System.out.println(">>> Usuário Aluno de teste criado: aluno1/senha123");
            createdAnyUser = true;
        } else {
            // Opcional: Poderia adicionar lógica similar à do admin para verificar/atualizar aluno1 se necessário
            System.out.println(">>> Usuário 'aluno1' já existe.");
        }

        // --- Usuário Pendente de Teste ---
        if (userRepository.findByUsername("pendente1").isEmpty()) {
            User pendingUser = new User();
            pendingUser.setNome("Usuário Pendente Exemplo");
            pendingUser.setUsername("pendente1");
            pendingUser.setPassword(passwordEncoder.encode("senha123"));
            pendingUser.setEmail("pendente@sistema.edu");
            pendingUser.setInstitution("Sistema Acadêmico");
            pendingUser.setRole(Role.PROFESSOR); // Papel inicial solicitado
            pendingUser.setStatus(AccountStatus.PENDENTE);
            pendingUser.setEnabled(false);
            // pendingUser.setEmailVerified(false); // Já é false por padrão
            pendingUser.setAdminComments("Aguardando aprovação do administrador (criado via DataInitializer)");
            userRepository.save(pendingUser);
            System.out.println(">>> Usuário Pendente de teste criado: pendente1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'pendente1' já existe.");
        }

        // Mensagem final
        if (!createdAnyUser && userRepository.count() >= 4) { // Verifica se nenhum foi criado E se os 4 principais existem
            System.out.println(">>> Todos os usuários de teste principais (admin, professor1, aluno1, pendente1) já existem no banco de dados.");
        }
        System.out.println(">>> DataInitializer finalizado.");
    }
}