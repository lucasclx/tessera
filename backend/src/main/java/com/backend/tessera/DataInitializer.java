package com.backend.tessera; // Pacote raiz

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
    private PasswordEncoder passwordEncoder; // Injete o PasswordEncoder configurado em SecurityConfig

    @Override
    public void run(String... args) throws Exception {
        // Só cria se não houver usuários, para evitar duplicatas em reinicializações
        if (userRepository.count() == 0) {
            User professor = new User("professor1", passwordEncoder.encode("senha123"), Role.PROFESSOR);
            User aluno = new User("aluno1", passwordEncoder.encode("senha123"), Role.ALUNO);

            userRepository.save(professor);
            userRepository.save(aluno);

            System.out.println(">>> Usuários de teste criados: professor1/senha123, aluno1/senha123");
        } else {
            System.out.println(">>> Usuários já existem no banco de dados. Nenhum usuário de teste foi criado.");
        }
    }
}