package com.backend.tessera;

import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Só cria se não houver usuários, para evitar duplicatas em reinicializações
        if (userRepository.count() == 0) {
            // Criar usuário admin (SEMPRE ATIVO) - necessário para o funcionamento do sistema
            User admin = new User();
            admin.setNome("Administrador");
            admin.setUsername("admin");
            admin.setEmail("admin@sistema.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setInstitution("Sistema Acadêmico");
            admin.setRole(Role.ADMIN);
            admin.setStatus(AccountStatus.ATIVO); // Admin está sempre ativo
            admin.setEnabled(true);
            
            // Criar usuário professor (ATIVO para testes)
            User professor = new User();
            professor.setNome("Professor Exemplo");
            professor.setUsername("professor1");
            professor.setEmail("professor@sistema.edu");
            professor.setPassword(passwordEncoder.encode("senha123"));
            professor.setInstitution("Sistema Acadêmico");
            professor.setRole(Role.PROFESSOR);
            professor.setStatus(AccountStatus.ATIVO); // Professor de teste já aprovado
            professor.setEnabled(true);
            
            // Criar usuário aluno (ATIVO para testes)
            User aluno = new User();
            aluno.setNome("Aluno Exemplo");
            aluno.setUsername("aluno1");
            aluno.setEmail("aluno@sistema.edu");
            aluno.setPassword(passwordEncoder.encode("senha123"));
            aluno.setInstitution("Sistema Acadêmico");
            aluno.setRole(Role.ALUNO);
            aluno.setStatus(AccountStatus.ATIVO); // Aluno de teste já aprovado
            aluno.setEnabled(true);

            // Criar usuário pendente de aprovação (para demonstração do processo)
            User pendingUser = new User();
            pendingUser.setNome("Usuário Pendente");
            pendingUser.setUsername("pendente1");
            pendingUser.setPassword(passwordEncoder.encode("senha123"));
            pendingUser.setEmail("pendente@sistema.edu");
            pendingUser.setInstitution("Sistema Acadêmico");
            pendingUser.setRole(Role.PROFESSOR); // Papel solicitado
            pendingUser.setStatus(AccountStatus.PENDENTE); // Status pendente
            pendingUser.setEnabled(true);
            pendingUser.setAdminComments("Aguardando aprovação do administrador");

            userRepository.save(admin);
            userRepository.save(professor);
            userRepository.save(aluno);
            userRepository.save(pendingUser);

            System.out.println(">>> Usuários de teste criados:");
            System.out.println("    Admin: admin/admin123");
            System.out.println("    Professor: professor1/senha123");
            System.out.println("    Aluno: aluno1/senha123");
            System.out.println("    Pendente: pendente1/senha123 (aguardando aprovação)");
        } else {
            System.out.println(">>> Usuários já existem no banco de dados. Nenhum usuário de teste foi criado.");
            
            // Verificar se já existe um administrador
            if (userRepository.findByUsername("admin").isEmpty()) {
                // Criar usuário admin caso não exista
                User admin = new User();
                admin.setNome("Administrador");
                admin.setUsername("admin");
                admin.setEmail("admin@sistema.edu");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setInstitution("Sistema Acadêmico");
                admin.setRole(Role.ADMIN);
                admin.setStatus(AccountStatus.ATIVO);
                admin.setEnabled(true);
                
                userRepository.save(admin);
                System.out.println(">>> Usuário Administrador criado: admin/admin123");
            }
        }
    }
}