package com.backend.tessera;

import com.backend.tessera.auth.entity.AccountStatus;
import com.backend.tessera.auth.entity.Role;
import com.backend.tessera.auth.entity.User;
import com.backend.tessera.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        boolean createdAnyUser = false;
        System.out.println(">>> Iniciando DataInitializer (alternativo)...");

        if (userRepository.findByUsername("admin_alt").isEmpty()) {
            User admin = new User(
                "Administrador Alternativo",
                "admin_alt",
                "admin_alt@sistema.edu",
                passwordEncoder.encode("admin123"),
                "Sistema Acadêmico Alternativo",
                Role.ADMIN,
                AccountStatus.ATIVO,
                true
            );
            admin.setAdminComments("Usuário administrador alternativo padrão.");
            // approvalDate será setado pelo @PrePersist na entidade User
            userRepository.save(admin);
            System.out.println(">>> Usuário Administrador Alternativo criado: admin_alt/admin123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'admin_alt' já existe.");
        }

        if (userRepository.findByUsername("professor_alt1").isEmpty()) {
            User professor = new User(
                "Professor Exemplo Alternativo",
                "professor_alt1",
                "professor_alt@sistema.edu",
                passwordEncoder.encode("senha123"),
                "Sistema Acadêmico Alternativo",
                Role.PROFESSOR,
                AccountStatus.ATIVO,
                true
            );
            professor.setAdminComments("Usuário professor de teste alternativo, aprovado.");
            userRepository.save(professor);
            System.out.println(">>> Usuário Professor de teste Alternativo criado: professor_alt1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'professor_alt1' já existe.");
        }

        if (userRepository.findByUsername("aluno_alt1").isEmpty()) {
            User aluno = new User(
                "Aluno Exemplo Alternativo",
                "aluno_alt1",
                "aluno_alt@sistema.edu",
                passwordEncoder.encode("senha123"),
                "Sistema Acadêmico Alternativo",
                Role.ALUNO,
                AccountStatus.ATIVO,
                true
            );
            aluno.setAdminComments("Usuário aluno de teste alternativo, aprovado.");
            userRepository.save(aluno);
            System.out.println(">>> Usuário Aluno de teste Alternativo criado: aluno_alt1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'aluno_alt1' já existe.");
        }

        if (userRepository.findByUsername("pendente_alt1").isEmpty()) {
            User pendingUser = new User(
                "Usuário Pendente Alternativo Exemplo",
                "pendente_alt1",
                "pendente_alt@sistema.edu",
                passwordEncoder.encode("senha123"),
                "Sistema Acadêmico Alternativo",
                Role.PROFESSOR,
                AccountStatus.PENDENTE,
                false
            );
            pendingUser.setAdminComments("Aguardando aprovação do administrador (DataInitializer Alternativo)");
            userRepository.save(pendingUser);
            System.out.println(">>> Usuário Pendente de teste Alternativo criado: pendente_alt1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'pendente_alt1' já existe.");
        }

        if (!createdAnyUser) {
            System.out.println(">>> Todos os usuários de teste alternativos principais já existem no banco de dados.");
        }
         System.out.println(">>> DataInitializer (alternativo) finalizado.");
    }
}