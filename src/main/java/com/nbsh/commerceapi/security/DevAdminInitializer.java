package com.nbsh.commerceapi.security;

import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import com.nbsh.commerceapi.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@Profile("dev")
public class DevAdminInitializer
        implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public DevAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.dev-admin.email}")
            String email,
            @Value("${app.dev-admin.password}")
            String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(
            ApplicationArguments args
    ) {

        String normalizedEmail =
                email.trim()
                        .toLowerCase(Locale.ROOT);

        User admin =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseGet(() ->
                                new User(
                                        normalizedEmail,
                                        passwordEncoder.encode(
                                                password
                                        ),
                                        "Development",
                                        "Admin",
                                        UserRole.ADMIN,
                                        true
                                )
                        );

        admin.setPasswordHash(
                passwordEncoder.encode(password)
        );

        admin.setRole(
                UserRole.ADMIN
        );

        admin.setActive(true);

        userRepository.save(admin);
    }
}