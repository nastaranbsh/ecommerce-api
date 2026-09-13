package com.nbsh.commerceapi.user;

import com.nbsh.commerceapi.security.CurrentUser;
import com.nbsh.commerceapi.user.dto.AddressResponse;
import com.nbsh.commerceapi.user.dto.CreateAddressRequest;
import com.nbsh.commerceapi.user.dto.UpdateProfileRequest;
import com.nbsh.commerceapi.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserService userService;
    private final AddressService addressService;
    private final CurrentUser currentUser;

    public MeController(
            UserService userService,
            AddressService addressService,
            CurrentUser currentUser
    ) {
        this.userService = userService;
        this.addressService = addressService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public UserResponse getProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return userService.getProfile(userId);
    }

    @PutMapping
    public UserResponse updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid
            @RequestBody
            UpdateProfileRequest request
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return userService.updateProfile(
                userId,
                request
        );
    }

    @GetMapping("/addresses")
    public List<AddressResponse> getAddresses(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return addressService
                .getAddresses(userId);
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse>
    createAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid
            @RequestBody
            CreateAddressRequest request
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        AddressResponse address =
                addressService.createAddress(
                        userId,
                        request
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/me/addresses/"
                                        + address.id()
                        )
                )
                .body(address);
    }
}