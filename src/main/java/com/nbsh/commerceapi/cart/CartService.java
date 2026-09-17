package com.nbsh.commerceapi.cart;

import com.nbsh.commerceapi.cart.dto.AddCartItemRequest;
import com.nbsh.commerceapi.cart.dto.CartItemResponse;
import com.nbsh.commerceapi.cart.dto.CartResponse;
import com.nbsh.commerceapi.cart.dto.UpdateCartItemRequest;
import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.inventory.InventoryService;
import com.nbsh.commerceapi.product.Product;
import com.nbsh.commerceapi.product.ProductRepository;
import com.nbsh.commerceapi.user.User;
import com.nbsh.commerceapi.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private static final int MAX_QUANTITY = 99;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            InventoryService inventoryService
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(
            Long userId
    ) {

        Cart cart = findCart(userId);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(
            Long userId,
            AddCartItemRequest request
    ) {

        Cart cart = findCart(userId);

        Product product =
                findAvailableProduct(
                        request.productId()
                );

        CartItem existingItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity()
                            + request.quantity();

            validateQuantity(newQuantity);

            inventoryService.assertAvailable(
                    product.getId(),
                    newQuantity
            );

            existingItem.setQuantity(
                    newQuantity
            );

        } else {

            inventoryService.assertAvailable(
                    product.getId(),
                    request.quantity()
            );

            CartItem item =
                    new CartItem(
                            cart,
                            product,
                            request.quantity()
                    );

            cartItemRepository.save(item);
        }

        return toResponse(
                findCart(userId)
        );
    }

    @Transactional
    public CartResponse updateItem(
            Long userId,
            Long itemId,
            UpdateCartItemRequest request
    ) {

        validateQuantity(
                request.quantity()
        );

        CartItem item =
                findOwnedCartItem(
                        itemId,
                        userId
                );

        if (!item.getProduct().isActive()) {
            throw new InvalidRequestException(
                    "Product is not currently available"
            );
        }

        inventoryService.assertAvailable(
                item.getProduct().getId(),
                request.quantity()
        );

        item.setQuantity(
                request.quantity()
        );

        return toResponse(
                findCart(userId)
        );
    }

    @Transactional
    public void removeItem(
            Long userId,
            Long itemId
    ) {

        CartItem item =
                findOwnedCartItem(
                        itemId,
                        userId
                );

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(
            Long userId
    ) {

        Cart cart = findCart(userId);

        cartItemRepository
                .deleteAllByCartId(
                        cart.getId()
                );
    }

    private Cart findCart(
            Long userId
    ) {

        return cartRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "User cart does not exist"
                        )
                );
    }

    private Product findAvailableProduct(
            Long productId
    ) {

        Product product =
                productRepository
                        .findById(productId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found with id: "
                                                + productId
                                )
                        );

        if (!product.isActive()) {
            throw new InvalidRequestException(
                    "Product is not currently available"
            );
        }

        return product;
    }

    private CartItem findOwnedCartItem(
            Long itemId,
            Long userId
    ) {

        return cartItemRepository
                .findByIdAndCartUserId(
                        itemId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found with id: "
                                        + itemId
                        )
                );
    }

    private void validateQuantity(
            int quantity
    ) {

        if (quantity < 1
                || quantity > MAX_QUANTITY) {

            throw new InvalidRequestException(
                    "Quantity must be between 1 and "
                            + MAX_QUANTITY
            );
        }
    }

    private CartResponse toResponse(
            Cart cart
    ) {

        List<CartItemResponse> items =
                cart.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        int totalQuantity =
                items.stream()
                        .mapToInt(
                                CartItemResponse::quantity
                        )
                        .sum();

        BigDecimal totalPrice =
                items.stream()
                        .map(
                                CartItemResponse::subtotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new CartResponse(
                cart.getId(),
                items,
                totalQuantity,
                totalPrice
        );
    }

    private CartItemResponse toItemResponse(
            CartItem item
    ) {

        Product product =
                item.getProduct();

        BigDecimal unitPrice =
                product.getPrice();

        BigDecimal subtotal =
                unitPrice.multiply(
                        BigDecimal.valueOf(
                                item.getQuantity()
                        )
                );

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getCategory().getName(),
                unitPrice,
                item.getQuantity(),
                subtotal
        );
    }
}





