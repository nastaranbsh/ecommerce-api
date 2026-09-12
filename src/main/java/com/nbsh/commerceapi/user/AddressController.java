package com.nbsh.commerceapi.user;

import com.nbsh.commerceapi.user.dto.AddressResponse;
import com.nbsh.commerceapi.user.dto.CreateAddressRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(
            AddressService addressService
    ) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody CreateAddressRequest request
    ) {

        AddressResponse address =
                addressService.createAddress(
                        userId,
                        request
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/v1/users/"
                                        + userId
                                        + "/addresses/"
                                        + address.id()
                        )
                )
                .body(address);
    }

    @GetMapping
    public List<AddressResponse> getAddresses(
            @PathVariable Long userId
    ) {

        return addressService.getAddresses(
                userId
        );
    }
}