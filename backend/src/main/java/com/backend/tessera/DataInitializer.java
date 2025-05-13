package com.backend.tessera;

import com.backend.tessera.config.LoggerConfig;
import com.backend.tessera.model.AccountStatus;
import com.backend.tessera.model.Role;
import com.backend.tessera.model.User;
import com.backend.tessera.repository.UserRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        boolean createdAnyUser = false;
        logger.info("Iniciando DataInitializer...");
        
        // --- Método refatorado para garantir que todos os usuários necessários existam ----
        createdAnyUser |= ensureUserExists(
            "admin", "admin123", "Administrador", "admin@sistema.edu", 
            "Sistema Acadêmico", Role.ADMIN, AccountStatus.ATIVO, true, true,
            "Usuário administrador padrão.");
            
        createdAnyUser |= ensureUserExists(
            "professor1", "senha123", "Professor Exemplo", "professor@sistema.edu", 
            "Sistema Acadêmico", Role.PROFESSOR, AccountStatus.ATIVO, true, true,
            "Usuário professor de teste, aprovado e verificado via Initializer.");
            
        createdAnyUser |= ensureUserExists(
            "aluno1", "senha123", "Aluno Exemplo", "aluno@sistema.edu", 
            "Sistema Acadêmico", Role.ALUNO, AccountStatus.ATIVO, true, true,
            "Usuário aluno de teste, aprovado e verificado via Initializer.");
            
        createdAnyUser |= ensureUserExists(
            "pendente1", "senha123", "Usuário Pendente Exemplo", "pendente@sistema.edu", 
            "Sistema Acadêmico", Role.PROFESSOR, AccountStatus.PENDENTE, false, false,
            "Aguardando aprovação do administrador (criado via DataInitializer)");
            
        // Mensagem final
        if (!createdAnyUser) {
            logger.info("Todos os usuários de teste principais já existem no banco de dados.");
        }
        
        logger.info("DataInitializer finalizado.");
    }
    
    /**
     * Garante que um usuário com o username especificado exista no sistema.
     * Se o usuário não existir, será criado. Se existir, seus atributos serão atualizados se necessário.
     * 
     * @return true se um novo usuário foi criado, false se o usuário já existia
     */
    private boolean ensureUserExists(String username, String password, String nome, String email, 
                                   String institution, Role role, AccountStatus status, 
                                   boolean enabled, boolean emailVerified, String adminComments) {
        
        Optional<User> existingUser = userRepository.findByUsername(username);
        
        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setNome(nome);
            newUser.setEmail(email);
            newUser.setInstitution(institution);
            newUser.setRole(role);
            newUser.setStatus(status);
            newUser.setEnabled(enabled);
            newUser.setEmailVerified(emailVerified);
            
            if (emailVerified) {
                newUser.setEmailVerifiedAt(LocalDateTime.now());
            }
            
            if (status == AccountStatus.ATIVO) {
                newUser.setApprovalDate(LocalDateTime.now());
            }
            
            newUser.setAdminComments(adminComments);
            
            userRepository.save(newUser);
            logger.info("Usuário '{}' criado com sucesso", username);
            return true;
        } else {
            User user = existingUser.get();
            boolean updated = false;
            
            // Verificar se precisamos atualizar algum atributo
            if (user.getStatus() != status) {
                user.setStatus(status);
                updated = true;
            }
            
            if (user.isEnabled() != enabled) {
                user.setEnabled(enabled);
                updated = true;
            }
            
            if (user.isEmailVerified() != emailVerified) {
                user.setEmailVerified(emailVerified);
                if (emailVerified) {
                    user.setEmailVerifiedAt(LocalDateTime.now());
                }
                updated = true;
            }
            
            if (updated) {
                userRepository.save(user);
                logger.info("Usuário '{}' atualizado", username);
            } else {
                logger.info("Usuário '{}' já existe e está configurado corretamente", username);
            }
            
            return false;
        }
    }
}