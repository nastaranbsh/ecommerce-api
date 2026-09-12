package com.nbsh.commerceapi.user;

import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.user.dto.AddressResponse;
import com.nbsh.commerceapi.user.dto.CreateAddressRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressService(
            AddressRepository addressRepository,
            UserService userService
    ) {
        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    @Transactional
    public AddressResponse createAddress(
            Long userId,
            CreateAddressRequest request
    ) {

        User user =
                userService.findUser(userId);

        boolean defaultAddress =
                request.defaultAddress() != null
                        && request.defaultAddress();

        if (defaultAddress) {
            clearCurrentDefaultAddress(userId);
        }

        Address address = new Address(
                user,
                normalizeNullable(request.label()),
                request.recipientName().trim(),
                request.line1().trim(),
                normalizeNullable(request.line2()),
                request.city().trim(),
                normalizeNullable(request.stateOrProvince()),
                request.postalCode().trim(),
                request.countryCode()
                        .trim()
                        .toUpperCase(Locale.ROOT),
                normalizeNullable(request.phone()),
                defaultAddress
        );

        Address saved =
                addressRepository.save(address);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(
            Long userId
    ) {

        userService.findUser(userId);

        return addressRepository
                .findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void clearCurrentDefaultAddress(
            Long userId
    ) {

        addressRepository.findAllByUserId(userId)
                .stream()
                .filter(Address::isDefaultAddress)
                .forEach(address ->
                        address.setDefaultAddress(false)
                );
    }

    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private AddressResponse toResponse(
            Address address
    ) {

        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getRecipientName(),
                address.getLine1(),
                address.getLine2(),
                address.getCity(),
                address.getStateOrProvince(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getPhone(),
                address.isDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}