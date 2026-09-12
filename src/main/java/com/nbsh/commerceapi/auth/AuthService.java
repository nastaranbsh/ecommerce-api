package com.nbsh.commerceapi.auth;

import com.nbsh.commerceapi.auth.dto.RegisterRequest;
import com.nbsh.commerceapi.auth.dto.RegisterResponse;
import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import com.nbsh.commerceapi.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(
            RegisterRequest request
    ) {

        String normalizedEmail =
                normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceConflictException(
                    "Email is already registered"
            );
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );

        User user = new User(
                normalizedEmail,
                passwordHash,
                request.firstName().trim(),
                request.lastName().trim(),
                UserRole.CUSTOMER,
                true
        );

        User savedUser =
                userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getCreatedAt()
        );
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}