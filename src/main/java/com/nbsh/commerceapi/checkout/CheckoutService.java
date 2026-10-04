package com.nbsh.commerceapi.checkout;

import com.nbsh.commerceapi.cart.Cart;
import com.nbsh.commerceapi.cart.CartItem;
import com.nbsh.commerceapi.cart.CartItemRepository;
import com.nbsh.commerceapi.cart.CartRepository;
import com.nbsh.commerceapi.common.exception.InsufficientStockException;
import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.inventory.Inventory;
import com.nbsh.commerceapi.inventory.InventoryRepository;
import com.nbsh.commerceapi.observability.CommerceMetrics;
import com.nbsh.commerceapi.order.Order;
import com.nbsh.commerceapi.order.OrderService;
import com.nbsh.commerceapi.order.dto.OrderResponse;
import com.nbsh.commerceapi.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckoutService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderService orderService;

    private final CommerceMetrics commerceMetrics;

    public CheckoutService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            InventoryRepository inventoryRepository,
            OrderService orderService,
            CommerceMetrics commerceMetrics
    ) {
        this.cartRepository =
                cartRepository;

        this.cartItemRepository =
                cartItemRepository;

        this.inventoryRepository =
                inventoryRepository;

        this.orderService =
                orderService;

        this.commerceMetrics = commerceMetrics;
    }

    @Transactional
    public OrderResponse checkout(
            Long userId
    ) {

        Instant startedAt =
                Instant.now();

        try {

            OrderResponse response =
                    performCheckout(
                            userId
                    );

            commerceMetrics
                    .recordCheckoutSuccess(
                            Duration.between(
                                    startedAt,
                                    Instant.now()
                            )
                    );

            return response;

        } catch (InsufficientStockException exception) {

            commerceMetrics
                    .recordCheckoutFailure(
                            "insufficient_stock",
                            Duration.between(
                                    startedAt,
                                    Instant.now()
                            )
                    );

            throw exception;

        } catch (InvalidRequestException exception) {

            commerceMetrics
                    .recordCheckoutFailure(
                            "invalid_request",
                            Duration.between(
                                    startedAt,
                                    Instant.now()
                            )
                    );

            throw exception;

        } catch (RuntimeException exception) {

            commerceMetrics
                    .recordCheckoutFailure(
                            "other",
                            Duration.between(
                                    startedAt,
                                    Instant.now()
                            )
                    );

            throw exception;
        }
    }

    private OrderResponse performCheckout(
            Long userId
    ) {
        Cart cart =
                findCartForUpdate(userId);

        List<CartItem> cartItems =
                cart.getItems()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        item ->
                                                item.getProduct()
                                                        .getId()
                                )
                        )
                        .toList();

        if (cartItems.isEmpty()) {
            throw new InvalidRequestException(
                    "Cart is empty"
            );
        }

        List<Long> productIds =
                cartItems.stream()
                        .map(item ->
                                item.getProduct()
                                        .getId()
                        )
                        .distinct()
                        .sorted()
                        .toList();

        List<Inventory> inventories =
                inventoryRepository
                        .findAllByProductIdsForUpdate(
                                productIds
                        );

        if (inventories.size()
                != productIds.size()) {

            throw new ResourceNotFoundException(
                    "Inventory information is incomplete"
            );
        }

        Map<Long, Inventory> inventoryByProductId =
                indexInventories(inventories);

        List<OrderService.OrderItemDraft> itemDrafts =
                cartItems.stream()
                        .map(item ->
                                createOrderItemDraft(
                                        item,
                                        inventoryByProductId
                                )
                        )
                        .toList();

        decreaseInventory(
                itemDrafts,
                inventoryByProductId
        );

        Order order =
                orderService.createOrder(
                        cart.getUser(),
                        itemDrafts
                );


        cartItemRepository.deleteAllByCartId(
                cart.getId()
        );

        return orderService
                .toOrderResponse(order);
    }

    private Cart findCartForUpdate(
            Long userId
    ) {

        return cartRepository
                .findByUserIdForUpdate(
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        )
                );
    }

    private Map<Long, Inventory> indexInventories(
            List<Inventory> inventories
    ) {

        Map<Long, Inventory> result =
                new HashMap<>();

        for (Inventory inventory : inventories) {

            result.put(
                    inventory.getProduct()
                            .getId(),
                    inventory
            );
        }

        return result;
    }

    private OrderService.OrderItemDraft createOrderItemDraft(
            CartItem item,
            Map<Long, Inventory> inventoryByProductId
    ) {

        Product product =
                item.getProduct();

        if (!product.isActive()) {
            throw new InvalidRequestException(
                    "Product is no longer available: "
                            + product.getName()
            );
        }

        Inventory inventory =
                inventoryByProductId
                        .get(product.getId());

        if (inventory == null) {
            throw new ResourceNotFoundException(
                    "Inventory not found for product id: "
                            + product.getId()
            );
        }

        int requestedQuantity =
                item.getQuantity();

        int availableQuantity =
                inventory.getQuantityAvailable();

        if (availableQuantity
                < requestedQuantity) {

            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + product.getName()
                            + ". Requested "
                            + requestedQuantity
                            + ", available "
                            + availableQuantity
            );
        }

        BigDecimal unitPrice =
                product.getPrice();

        BigDecimal lineTotal =
                unitPrice.multiply(
                        BigDecimal.valueOf(
                                requestedQuantity
                        )
                );

        return new OrderService.OrderItemDraft(
                product,
                unitPrice,
                requestedQuantity,
                lineTotal
        );
    }

    private void decreaseInventory(
            List<OrderService.OrderItemDraft> itemDrafts,
            Map<Long, Inventory> inventoryByProductId
    ) {

        for (OrderService.OrderItemDraft draft : itemDrafts) {

            Long productId =
                    draft.product()
                            .getId();

            Inventory inventory =
                    inventoryByProductId
                            .get(productId);

            inventory.setQuantityAvailable(
                    inventory.getQuantityAvailable()
                            - draft.quantity()
            );
        }
    }
}