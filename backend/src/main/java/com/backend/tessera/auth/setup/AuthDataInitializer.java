package com.backend.tessera.auth.setup;

import com.backend.tessera.auth.entity.AccountStatus;
import com.backend.tessera.auth.entity.Role;
import com.backend.tessera.auth.entity.User;
import com.backend.tessera.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime; // Necessário para approvalDate

@Component
public class AuthDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        boolean createdAnyUser = false;
        System.out.println(">>> Iniciando AuthDataInitializer...");

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                "Administrador",
                "admin",
                "admin@sistema.edu",
                passwordEncoder.encode("admin123"),
                "Sistema Acadêmico",
                Role.ADMIN,
                AccountStatus.ATIVO,
                true // enabled
            );
            admin.setAdminComments("Usuário administrador padrão.");
            // approvalDate será setado pelo @PrePersist
            userRepository.save(admin);
            System.out.println(">>> Usuário Administrador criado: admin/admin123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'admin' já existe.");
        }

        if (userRepository.findByUsername("professor1").isEmpty()) {
            User professor = new User(
                "Professor Exemplo",
                "professor1",
                "professor@sistema.edu",
                passwordEncoder.encode("senha123"),
                "Sistema Acadêmico",
                Role.PROFESSOR,
                AccountStatus.ATIVO,
                true // enabled
            );
            professor.setAdminComments("Usuário professor de teste, aprovado.");
            userRepository.save(professor);
            System.out.println(">>> Usuário Professor de teste criado: professor1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'professor1' já existe.");
        }

        if (userRepository.findByUsername("aluno1").isEmpty()) {
            User aluno = new User(
                "Aluno Exemplo",
                "aluno1",
                "aluno@sistema.edu",
                passwordEncoder.encode("senha123"),
                "Sistema Acadêmico",
                Role.ALUNO,
                AccountStatus.ATIVO,
                true // enabled
            );
            aluno.setAdminComments("Usuário aluno de teste, aprovado.");
            userRepository.save(aluno);
            System.out.println(">>> Usuário Aluno de teste criado: aluno1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'aluno1' já existe.");
        }

        if (userRepository.findByUsername("pendente1").isEmpty()) {
            User pendingUser = new User(
                "Usuário Pendente Exemplo",
                "pendente1",
                "pendente@sistema.edu",
                passwordEncoder.encode("senha123"),
                "Sistema Acadêmico",
                Role.PROFESSOR, // Role pode ser qualquer um, já que está pendente
                AccountStatus.PENDENTE,
                false // enabled
            );
            pendingUser.setAdminComments("Aguardando aprovação do administrador (AuthDataInitializer)");
            userRepository.save(pendingUser);
            System.out.println(">>> Usuário Pendente de teste criado: pendente1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'pendente1' já existe.");
        }

        if (!createdAnyUser) {
            System.out.println(">>> Todos os usuários de teste principais (admin, professor1, aluno1, pendente1) já existem no banco de dados.");
        }
         System.out.println(">>> AuthDataInitializer finalizado.");
    }
}
