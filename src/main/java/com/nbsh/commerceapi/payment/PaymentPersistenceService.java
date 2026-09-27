package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.common.exception.InvalidOrderStateException;
import com.nbsh.commerceapi.common.exception.ResourceConflictException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.inventory.Inventory;
import com.nbsh.commerceapi.inventory.InventoryRepository;
import com.nbsh.commerceapi.order.Order;
import com.nbsh.commerceapi.order.OrderItem;
import com.nbsh.commerceapi.order.OrderRepository;
import com.nbsh.commerceapi.order.OrderStatus;
import com.nbsh.commerceapi.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentPersistenceService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public PaymentPersistenceService(
            PaymentAttemptRepository paymentAttemptRepository,
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository
    ) {
        this.paymentAttemptRepository =
                paymentAttemptRepository;

        this.orderRepository =
                orderRepository;

        this.inventoryRepository =
                inventoryRepository;
    }

    @Transactional
    public PreparedPayment preparePayment(
            Long userId,
            Long orderId,
            String idempotencyKey
    ) {

        Order order =
                orderRepository
                        .findByIdAndUserIdForUpdate(
                                orderId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        PaymentAttempt existing =
                paymentAttemptRepository
                        .findByIdempotencyKey(
                                idempotencyKey
                        )
                        .orElse(null);

        if (existing != null) {

            if (!existing.getOrder()
                    .getId()
                    .equals(orderId)) {

                throw new ResourceConflictException(
                        "Idempotency-Key has already been used for a different payment operation"
                );
            }

            return toPreparedPayment(
                    existing
            );
        }

        if (order.getStatus()
                != OrderStatus.PENDING) {

            throw new InvalidOrderStateException(
                    "Only PENDING orders can be paid"
            );
        }

        boolean otherActiveAttempt =
                paymentAttemptRepository
                        .existsByOrderIdAndStatusIn(
                                orderId,
                                List.of(
                                        PaymentStatus.INITIATED,
                                        PaymentStatus.UNKNOWN,
                                        PaymentStatus.SUCCEEDED
                                )
                        );

        if (otherActiveAttempt) {

            throw new ResourceConflictException(
                    "This order already has a payment operation. Retry using its original Idempotency-Key."
            );
        }

        PaymentAttempt attempt =
                new PaymentAttempt(
                        order,
                        order.getTotalAmount(),
                        idempotencyKey
                );

        PaymentAttempt saved =
                paymentAttemptRepository.save(
                        attempt
                );

        return toPreparedPayment(saved);
    }

    @Transactional
    public PaymentResponse markSucceeded(
            Long paymentAttemptId,
            String gatewayReference
    ) {

        PaymentAttempt attempt =
                findAttemptForUpdate(
                        paymentAttemptId
                );

        ensureFinalizable(attempt);

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                attempt.getOrder().getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (order.getStatus()
                != OrderStatus.PENDING) {

            throw new InvalidOrderStateException(
                    "Order is no longer awaiting payment"
            );
        }

        attempt.markSucceeded(
                gatewayReference
        );

        order.changeStatus(
                OrderStatus.PAID
        );

        return new PaymentResponse(
                attempt.getId(),
                order.getId(),
                order.getOrderNumber(),
                attempt.getStatus(),
                gatewayReference,
                "Payment succeeded",
                false
        );
    }

    @Transactional
    public PaymentResponse markDeclinedAndRestoreStock(
            Long paymentAttemptId,
            String failureCode,
            String failureMessage
    ) {

        PaymentAttempt attempt =
                findAttemptForUpdate(
                        paymentAttemptId
                );

        ensureFinalizable(attempt);

        Order order =
                orderRepository
                        .findByIdForUpdate(
                                attempt.getOrder().getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found"
                                )
                        );

        if (order.getStatus()
                != OrderStatus.PENDING) {

            throw new InvalidOrderStateException(
                    "Order is no longer awaiting payment"
            );
        }

        restoreInventory(order);

        attempt.markDeclined(
                failureCode,
                failureMessage
        );

        order.changeStatus(
                OrderStatus.PAYMENT_FAILED
        );

        return new PaymentResponse(
                attempt.getId(),
                order.getId(),
                order.getOrderNumber(),
                attempt.getStatus(),
                null,
                failureMessage,
                false
        );
    }

    @Transactional
    public void markUnknown(
            Long paymentAttemptId,
            String message
    ) {
        PaymentAttempt attempt =
                findAttemptForUpdate(
                        paymentAttemptId
                );

        if (attempt.getStatus()
                == PaymentStatus.UNKNOWN) {
            return;
        }

        if (attempt.getStatus()
                != PaymentStatus.INITIATED) {

            throw new ResourceConflictException(
                    "Payment attempt has already been finalized"
            );
        }

        attempt.markUnknown(message);
    }

    private PreparedPayment toPreparedPayment(
            PaymentAttempt attempt
    ) {

        Order order =
                attempt.getOrder();

        return new PreparedPayment(
                attempt.getId(),
                order.getId(),
                order.getOrderNumber(),
                attempt.getAmount(),
                attempt.getStatus(),
                attempt.getGatewayReference(),
                attempt.getFailureCode(),
                attempt.getFailureMessage()
        );
    }

    private PaymentAttempt findAttemptForUpdate(
            Long paymentAttemptId
    ) {

        return paymentAttemptRepository
                .findByIdForUpdate(
                        paymentAttemptId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment attempt not found with id: "
                                        + paymentAttemptId
                        )
                );
    }

    private void ensureFinalizable(
            PaymentAttempt attempt
    ) {

        if (attempt.getStatus()
                != PaymentStatus.INITIATED
                && attempt.getStatus()
                != PaymentStatus.UNKNOWN) {

            throw new ResourceConflictException(
                    "Payment attempt has already been finalized"
            );
        }
    }

    private void restoreInventory(
            Order order
    ) {

        List<OrderItem> items =
                order.getItems();

        boolean missingProduct =
                items.stream()
                        .anyMatch(item ->
                                item.getProduct() == null
                        );

        if (missingProduct) {
            throw new IllegalStateException(
                    "Cannot restore inventory because an order product no longer exists"
            );
        }

        List<Long> productIds =
                items.stream()
                        .map(OrderItem::getProduct)
                        .filter(product -> product != null)
                        .map(product -> product.getId())
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

            throw new IllegalStateException(
                    "Inventory information is incomplete"
            );
        }

        Map<Long, Inventory> inventoryByProductId =
                new HashMap<>();

        for (Inventory inventory : inventories) {

            inventoryByProductId.put(
                    inventory.getProduct()
                            .getId(),
                    inventory
            );
        }

        for (OrderItem item : items) {

            Long productId =
                    item.getProduct()
                            .getId();

            Inventory inventory =
                    inventoryByProductId
                            .get(productId);

            inventory.setQuantityAvailable(
                    inventory.getQuantityAvailable()
                            + item.getQuantity()
            );
        }
    }

    public record PreparedPayment(
            Long paymentAttemptId,
            Long orderId,
            String orderNumber,
            BigDecimal amount,
            PaymentStatus status,
            String gatewayReference,
            String failureCode,
            String failureMessage
    ) {
    }
}