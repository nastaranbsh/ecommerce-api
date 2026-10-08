package com.nbsh.commerceapi.support;

import com.nbsh.commerceapi.user.*;
import com.nbsh.commerceapi.category.*;
import com.nbsh.commerceapi.product.*;
import com.nbsh.commerceapi.inventory.*;
import com.nbsh.commerceapi.cart.*;
import com.nbsh.commerceapi.order.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class TestDataFactory {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    OrderRepository orderRepository;

    public User createCustomer(String email) {
        return userRepository.save(
                new User(
                        email,
                        "$2a$10$notarealhash",
                        "Test",
                        "User",
                        UserRole.CUSTOMER,
                        true
                ));
    }

    public Product createProduct(
            String name,
            String sku,
            String price,
            int stock
    ) {
        Category c = categoryRepository.save(new Category(
                "Category-" + UUID.randomUUID(),
                "slug-" + UUID.randomUUID(),
                "Test",
                true
        ));
        Product p = productRepository.save(new Product(
                name,
                "Test",
                new BigDecimal(price),
                sku,
                true,
                c
        ));
        inventoryRepository.save(new Inventory(
                p,
                stock
        ));
        return p;
    }

    public Cart createCart(User user) {
        return cartRepository.save(new Cart(user));
    }

    public CartItem addToCart(
            Cart cart,
            Product product,
            int qty
    ) {
        return cartItemRepository.save(new CartItem(
                cart,
                product,
                qty
        ));
    }

    public Order createOrder(
            User user,
            Product product,
            OrderStatus status
    ) {
        Order o = new Order(
                "ORD-" + UUID.randomUUID(),
                user,
                product.getPrice(),
                product.getPrice()
        );
        o.addItem(new OrderItem(
                o,
                product,
                product.getName(),
                product.getSku(),
                product.getPrice(),
                1,
                product.getPrice()
        ));
        if (status != OrderStatus.PENDING) {
            o.changeStatus(status);
        }
        return orderRepository.save(o);
    }
}
