package com.nbsh.commerceapi.order;

import com.nbsh.commerceapi.common.api.PageResponse;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.order.dto.OrderItemResponse;
import com.nbsh.commerceapi.order.dto.OrderResponse;
import com.nbsh.commerceapi.order.dto.OrderSummaryResponse;
import com.nbsh.commerceapi.order.dto.UpdateOrderStatusRequest;
import com.nbsh.commerceapi.product.Product;
import com.nbsh.commerceapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderService(
            OrderRepository orderRepository,
            OrderNumberGenerator orderNumberGenerator
    ) {
        this.orderRepository =
                orderRepository;

        this.orderNumberGenerator =
                orderNumberGenerator;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderForUser(
            Long userId,
            Long orderId
    ) {

        Order order =
                orderRepository
                        .findByIdAndUserId(
                                orderId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getOrdersForUser(
            Long userId,
            Pageable pageable
    ) {

        Page<Order> page =
                orderRepository
                        .findAllByUserId(
                                userId,
                                pageable
                        );

        List<OrderSummaryResponse> content =
                page.getContent()
                        .stream()
                        .map(this::toSummaryResponse)
                        .toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public OrderResponse getOrderForAdmin(
            Long orderId
    ) {

        Order order =
                orderRepository
                        .findWithItemsById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        return toResponse(order);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateStatus(
            Long orderId,
            UpdateOrderStatusRequest request
    ) {

        Order order =
                orderRepository
                        .findWithItemsById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        order.changeStatus(
                request.status()
        );

        return toResponse(order);
    }

    @Transactional
    public Order createOrder(
            User user,
            List<OrderItemDraft> itemDrafts
    ) {

        BigDecimal subtotal =
                itemDrafts.stream()
                        .map(
                                OrderItemDraft::lineTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalAmount = subtotal;

        Order order =
                new Order(
                        orderNumberGenerator.generate(),
                        user,
                        subtotal,
                        totalAmount
                );

        for (OrderItemDraft draft : itemDrafts) {

            Product product =
                    draft.product();

            OrderItem item =
                    new OrderItem(
                            order,
                            product,
                            product.getName(),
                            product.getSku(),
                            draft.unitPrice(),
                            draft.quantity(),
                            draft.lineTotal()
                    );

            order.addItem(item);
        }

        return orderRepository.save(order);
    }

    private OrderResponse toResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getSubtotal(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(
            OrderItem item
    ) {

        Long productId =
                item.getProduct() == null
                        ? null
                        : item.getProduct().getId();

        return new OrderItemResponse(
                item.getId(),
                productId,
                item.getProductName(),
                item.getProductSku(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }

    private OrderSummaryResponse toSummaryResponse(
            Order order
    ) {

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }

    public record OrderItemDraft(
            Product product,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }

    public OrderResponse toOrderResponse(
            Order order
    ) {
        return toResponse(order);
    }

}