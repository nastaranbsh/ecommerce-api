package com.nbsh.commerceapi.auth;

import com.nbsh.commerceapi.auth.dto.LoginRequest;
import com.nbsh.commerceapi.auth.dto.LoginResponse;
import com.nbsh.commerceapi.auth.dto.RegisterRequest;
import com.nbsh.commerceapi.auth.dto.RegisterResponse;
import com.nbsh.commerceapi.common.exception.InvalidCredentialsException;
import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.security.JwtService;
import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import com.nbsh.commerceapi.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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

    public LoginResponse login(
            LoginRequest request
    ) {

        String normalizedEmail =
                normalizeEmail(request.email());

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.password()
                    )
            );

        } catch (AuthenticationException exception) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid email or password"
                                )
                        );

        JwtService.TokenResult token =
                jwtService.generateAccessToken(user);

        return new LoginResponse(
                token.token(),
                "Bearer",
                token.expiresIn()
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