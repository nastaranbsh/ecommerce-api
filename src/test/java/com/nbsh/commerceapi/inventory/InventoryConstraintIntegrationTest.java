package com.nbsh.commerceapi.inventory;

import com.nbsh.commerceapi.support.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.*;

class InventoryConstraintIntegrationTest extends PostgresIntegrationTest {
    @Autowired
    TestDataFactory dataFactory;

    @Test
    void databaseRejectsNegativeInventory() {
        var product = dataFactory.createProduct(
                "Item",
                "STOCK-CHECK",
                "10.00",
                2
        );

        assertThatThrownBy(() ->
                jdbc.update(
                        "UPDATE inventories SET quantity_available=-1 WHERE product_id=?",
                        product.getId()
                )).isInstanceOf(DataIntegrityViolationException.class);
    }
}
