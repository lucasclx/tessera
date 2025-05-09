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
        boolean createdAnyUser = false;
        System.out.println(">>> Iniciando DataInitializer...");

        // Criar usuário admin se não existir
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setNome("Administrador");
            admin.setUsername("admin");
            admin.setEmail("admin@sistema.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setInstitution("Sistema Acadêmico");
            admin.setRole(Role.ADMIN);
            admin.setStatus(AccountStatus.ATIVO);
            admin.setEnabled(true);
            admin.setAdminComments("Usuário administrador padrão."); // Adicionando comentário
            userRepository.save(admin);
            System.out.println(">>> Usuário Administrador criado: admin/admin123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'admin' já existe.");
        }

        // Criar usuário professor de teste se não existir
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
            professor.setAdminComments("Usuário professor de teste, aprovado."); // Adicionando comentário
            userRepository.save(professor);
            System.out.println(">>> Usuário Professor de teste criado: professor1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'professor1' já existe.");
        }

        // Criar usuário aluno de teste se não existir
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
            aluno.setAdminComments("Usuário aluno de teste, aprovado."); // Adicionando comentário
            userRepository.save(aluno);
            System.out.println(">>> Usuário Aluno de teste criado: aluno1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'aluno1' já existe.");
        }

        // Criar usuário pendente de teste se não existir
        if (userRepository.findByUsername("pendente1").isEmpty()) {
            User pendingUser = new User();
            pendingUser.setNome("Usuário Pendente Exemplo");
            pendingUser.setUsername("pendente1");
            pendingUser.setPassword(passwordEncoder.encode("senha123"));
            pendingUser.setEmail("pendente@sistema.edu");
            pendingUser.setInstitution("Sistema Acadêmico");
            pendingUser.setRole(Role.PROFESSOR); 
            pendingUser.setStatus(AccountStatus.PENDENTE); 
            pendingUser.setEnabled(false); 
            pendingUser.setAdminComments("Aguardando aprovação do administrador (DataInitializer)");
            userRepository.save(pendingUser);
            System.out.println(">>> Usuário Pendente de teste criado: pendente1/senha123");
            createdAnyUser = true;
        } else {
            System.out.println(">>> Usuário 'pendente1' já existe.");
        }

        if (!createdAnyUser) {
            // Esta mensagem agora só aparecerá se todos os 4 usuários acima já existirem.
            // A mensagem original "Usuários já existem no banco de dados. Nenhum usuário de teste foi criado."
            // baseada em userRepository.count() > 0 era menos precisa.
            System.out.println(">>> Todos os usuários de teste principais (admin, professor1, aluno1, pendente1) já existem no banco de dados.");
        }
         System.out.println(">>> DataInitializer finalizado.");
    }
}