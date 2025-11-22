package com.example.bbs.config;

import com.example.bbs.domain.UserAccount;
import com.example.bbs.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserDataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserAccountRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            UserAccount user1 = new UserAccount();
            user1.setUsername("user1");
            user1.setPassword(encoder.encode("password1"));
            user1.setRole("USER");
            repository.save(user1);

            UserAccount user2 = new UserAccount();
            user2.setUsername("user2");
            user2.setPassword(encoder.encode("password2"));
            user2.setRole("USER");
            repository.save(user2);

            UserAccount admin = new UserAccount();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("adminpass"));
            admin.setRole("ADMIN");
            repository.save(admin);
        };
    }
}
