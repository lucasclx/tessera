package com.backend.tessera;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerConfig.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.init.data:true}")
    private boolean shouldInitializeData;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!shouldInitializeData) {
            logger.info("Inicialização de dados desabilitada por configuração.");
            return;
        }
        
        boolean createdAnyUser = false;
        logger.info("Iniciando DataInitializer...");
        
        createdAnyUser |= ensureUserExists(
            "admin", "admin123", "Administrador", "admin@sistema.edu", 
            "Sistema Acadêmico", Role.ADMIN, AccountStatus.ATIVO, true,
            "Usuário administrador padrão.");
            
        createdAnyUser |= ensureUserExists(
            "professor1", "senha123", "Professor Exemplo", "professor@sistema.edu", 
            "Sistema Acadêmico", Role.PROFESSOR, AccountStatus.ATIVO, true,
            "Usuário professor de teste, aprovado e verificado via Initializer.");
            
        createdAnyUser |= ensureUserExists(
            "aluno1", "senha123", "Aluno Exemplo", "aluno@sistema.edu", 
            "Sistema Acadêmico", Role.ALUNO, AccountStatus.ATIVO, true,
            "Usuário aluno de teste, aprovado e verificado via Initializer.");
            
        createdAnyUser |= ensureUserExists(
            "pendente1", "senha123", "Usuário Pendente Exemplo", "pendente@sistema.edu", 
            "Sistema Acadêmico", Role.PROFESSOR, AccountStatus.PENDENTE, false,
            "Aguardando aprovação do administrador (criado via DataInitializer)");
            
        if (!createdAnyUser) {
            logger.info("Todos os usuários de teste principais já existem no banco de dados e estão configurados corretamente.");
        }
        
        logger.info("DataInitializer finalizado.");
    }
    
    private boolean ensureUserExists(String username, String password, String nome, String email, 
                                   String institution, Role role, AccountStatus status, 
                                   boolean emailVerified, String adminComments) {
        
        Optional<User> existingUserOpt = userRepository.findByUsername(username);
        
        if (existingUserOpt.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setNome(nome);
            newUser.setEmail(email);
            newUser.setInstitution(institution);
            newUser.setRole(role);
            // setStatus irá definir 'status' e 'enabled' consistentemente
            newUser.setStatus(status); 
            newUser.setEmailVerified(emailVerified);
            
            if (emailVerified) {
                newUser.setEmailVerifiedAt(LocalDateTime.now());
            }
            
            // Se o status for ATIVO, a data de aprovação é agora (consistente com enabled)
            // O setter de setStatus já cuida da data de aprovação se status for ATIVO e approvalDate for null
            // if (status == AccountStatus.ATIVO) {
            //     newUser.setApprovalDate(LocalDateTime.now());
            // }
            
            newUser.setAdminComments(adminComments);
            
            userRepository.save(newUser);
            logger.info("Usuário '{}' criado com sucesso. Status: {}, Enabled: {}", 
                        username, newUser.getStatus(), newUser.isEnabled());
            return true;
        } else {
            User user = existingUserOpt.get();
            boolean updated = false;
            
            if (user.getStatus() != status) {
                user.setStatus(status); // Atualiza status e enabled
                updated = true;
            }
            // Se a senha mudou (improvável para dados de inicialização, mas para completude)
            // if (!passwordEncoder.matches(password, user.getPassword())) {
            //    user.setPassword(passwordEncoder.encode(password));
            //    updated = true;
            // }
            if (user.isEmailVerified() != emailVerified) {
                user.setEmailVerified(emailVerified);
                if (emailVerified && user.getEmailVerifiedAt() == null) {
                    user.setEmailVerifiedAt(LocalDateTime.now());
                }
                updated = true;
            }
            if (user.getRole() != role){
                user.setRole(role);
                updated = true;
            }

            // Adicione outras verificações e atualizações se necessário
            
            if (updated) {
                userRepository.save(user);
                logger.info("Usuário '{}' atualizado. Status: {}, Enabled: {}", 
                            username, user.getStatus(), user.isEnabled());
            } else {
                logger.info("Usuário '{}' já existe e está configurado corretamente. Status: {}, Enabled: {}", 
                            username, user.getStatus(), user.isEnabled());
            }
            return false;
        }
    }
}