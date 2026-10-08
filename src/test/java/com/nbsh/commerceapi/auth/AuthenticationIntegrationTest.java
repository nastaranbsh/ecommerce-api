package com.nbsh.commerceapi.auth;

import com.nbsh.commerceapi.support.ApiIntegrationTest;
import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationIntegrationTest extends ApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registrationHashesPassword() throws Exception {

        String email = "registration@auth.test";
        String rawPassword = "StrongPass123!";

        mockMvc
                .perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s",
                                          "firstName": "Alice",
                                          "lastName": "Smith"
                                        }
                                        """.formatted(
                                        email,
                                        rawPassword
                                ))
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        header().string(
                                "Location",
                                org.hamcrest.Matchers.containsString(
                                        "/api/v1/users/"
                                )
                        )
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(email)
                )
                .andExpect(
                        jsonPath("$.firstName")
                                .value("Alice")
                )
                .andExpect(
                        jsonPath("$.lastName")
                                .value("Smith")
                )
                .andExpect(
                        jsonPath("$.password")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );

        User savedUser =
                userRepository
                        .findByEmail(email)
                        .orElseThrow();

        assertThat(
                savedUser.getPasswordHash()
        ).isNotEqualTo(
                rawPassword
        );

        assertThat(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPasswordHash()
                )
        ).isTrue();
    }

    @Test
    void loginIssuesToken() throws Exception {

        String email = "login@auth.test";
        String password = "StrongPass123!";

        register(
                email,
                password
        );

        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(
                                        email,
                                        password
                                ))
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiresIn")
                                .isNumber()
                );
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized()
            throws Exception {

        String email = "wrong-password@auth.test";
        String password = "StrongPass123!";

        register(
                email,
                password
        );

        mockMvc
                .perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "DefinitelyWrong123!"
                                        }
                                        """.formatted(email))
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void issuedTokenCanAccessProtectedEndpoint()
            throws Exception {

        String email = "protected-endpoint@auth.test";
        String password = "StrongPass123!";

        register(
                email,
                password
        );

        MvcResult loginResult =
                mockMvc
                        .perform(
                                post("/api/v1/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content("""
                                                {
                                                  "email": "%s",
                                                  "password": "%s"
                                                }
                                                """.formatted(
                                                email,
                                                password
                                        ))
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andExpect(
                                jsonPath("$.accessToken")
                                        .isNotEmpty()
                        )
                        .andReturn();

        String responseBody =
                loginResult
                        .getResponse()
                        .getContentAsString();

        JsonNode json =
                objectMapper.readTree(
                        responseBody
                );

        String accessToken =
                json
                        .get(
                                "accessToken"
                        )
                        .asString();

        assertThat(
                accessToken
        ).isNotBlank();

        mockMvc
                .perform(
                        get("/api/v1/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(email)
                )
                .andExpect(
                        jsonPath("$.firstName")
                                .value("Alice")
                )
                .andExpect(
                        jsonPath("$.lastName")
                                .value("Smith")
                );
    }

    private void register(
            String email,
            String password
    ) throws Exception {

        mockMvc
                .perform(
                        post("/api/v1/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s",
                                          "firstName": "Alice",
                                          "lastName": "Smith"
                                        }
                                        """.formatted(
                                        email,
                                        password
                                ))
                )
                .andExpect(
                        status().isCreated()
                );
    }
}