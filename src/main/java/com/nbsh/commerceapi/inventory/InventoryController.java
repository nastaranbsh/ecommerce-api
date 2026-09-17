package com.nbsh.commerceapi.inventory;

import com.nbsh.commerceapi.inventory.dto.InventoryResponse;
import com.nbsh.commerceapi.inventory.dto.UpdateInventoryRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        "/api/v1/admin/inventory/products"
)
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService =
                inventoryService;
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(
            @PathVariable Long productId
    ) {

        return inventoryService
                .getInventory(productId);
    }

    @PutMapping("/{productId}")
    public InventoryResponse updateInventory(
            @PathVariable Long productId,
            @Valid
            @RequestBody
            UpdateInventoryRequest request
    ) {

        return inventoryService
                .updateInventory(
                        productId,
                        request
                );
    }
}