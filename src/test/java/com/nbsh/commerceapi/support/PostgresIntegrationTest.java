package com.nbsh.commerceapi.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
//@Testcontainers
@SpringBootTest
//@AutoConfigureMockMvc
public abstract class PostgresIntegrationTest {

//    @Container --> when the container lifecycle belongs cleanly to one test class
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

        static {
        postgres.start();
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute(
                """
                TRUNCATE TABLE
                    payment_attempts,
                    reviews,
                    order_items,
                    orders,
                    cart_items,
                    carts,
                    inventories,
                    products,
                    categories,
                    addresses,
                    users
                RESTART IDENTITY CASCADE
                """
        );
    }
}