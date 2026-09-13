package com.nbsh.commerceapi.security;

import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username
    ) throws UsernameNotFoundException {

        String normalizedEmail =
                username
                        .trim()
                        .toLowerCase(Locale.ROOT);

        User user =
                userRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Invalid credentials"
                                )
                        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(
                        "ROLE_" + user.getRole().name()
                )
                .disabled(!user.isActive())
                .build();
    }
}