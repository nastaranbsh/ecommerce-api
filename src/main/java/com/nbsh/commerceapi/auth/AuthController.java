package com.nbsh.commerceapi.auth;

import com.nbsh.commerceapi.auth.dto.RegisterRequest;
import com.nbsh.commerceapi.auth.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/users/" + response.id()
                        )
                )
                .body(response);
    }
}