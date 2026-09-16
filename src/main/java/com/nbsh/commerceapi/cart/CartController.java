package com.nbsh.commerceapi.cart;

import com.nbsh.commerceapi.cart.dto.AddCartItemRequest;
import com.nbsh.commerceapi.cart.dto.CartResponse;
import com.nbsh.commerceapi.cart.dto.UpdateCartItemRequest;
import com.nbsh.commerceapi.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUser currentUser;

    public CartController(
            CartService cartService,
            CurrentUser currentUser
    ) {
        this.cartService = cartService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public CartResponse getCart(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return cartService.getCart(
                userId
        );
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid
            @RequestBody
            AddCartItemRequest request
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return cartService.addItem(
                userId,
                request
        );
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long itemId,
            @Valid
            @RequestBody
            UpdateCartItemRequest request
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return cartService.updateItem(
                userId,
                itemId,
                request
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long itemId
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        cartService.removeItem(
                userId,
                itemId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        cartService.clearCart(
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}