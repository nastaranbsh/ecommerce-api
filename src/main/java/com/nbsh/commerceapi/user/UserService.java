package com.nbsh.commerceapi.user;

import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.user.dto.UpdateUserRequest;
import com.nbsh.commerceapi.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {

        return toResponse(
                findUser(id)
        );
    }

    @Transactional
    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    ) {

        User user = findUser(id);

        String normalizedEmail =
                normalizeEmail(request.email());

        if (!user.getEmail().equals(normalizedEmail)
                && userRepository.existsByEmail(normalizedEmail)) {

            throw new ResourceConflictException(
                    "Email is already in use"
            );
        }

        user.setEmail(normalizedEmail);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setActive(request.active());

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public User findUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }

    public String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}