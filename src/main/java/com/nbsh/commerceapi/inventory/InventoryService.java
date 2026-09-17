package com.nbsh.commerceapi.inventory;

import com.nbsh.commerceapi.common.exception.InsufficientStockException;
import com.nbsh.commerceapi.common.exception.ResourceNotFoundException;
import com.nbsh.commerceapi.inventory.dto.InventoryResponse;
import com.nbsh.commerceapi.inventory.dto.UpdateInventoryRequest;
import com.nbsh.commerceapi.product.Product;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(
            InventoryRepository inventoryRepository
    ) {
        this.inventoryRepository =
                inventoryRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(
            Long productId
    ) {

        return toResponse(
                findInventory(productId)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public InventoryResponse updateInventory(
            Long productId,
            UpdateInventoryRequest request
    ) {

        Inventory inventory =
                findInventory(productId);

        inventory.setQuantityAvailable(
                request.quantityAvailable()
        );

        return toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public void assertAvailable(
            Long productId,
            int requestedQuantity
    ) {

        Inventory inventory =
                findInventory(productId);

        if (inventory.getQuantityAvailable()
                < requestedQuantity) {

            throw new InsufficientStockException(
                    "Requested quantity "
                            + requestedQuantity
                            + " exceeds available stock "
                            + inventory.getQuantityAvailable()
            );
        }
    }

    @Transactional
    public void decreaseStock(
            Long productId,
            int quantity
    ) {

        Inventory inventory =
                inventoryRepository
                        .findByProductIdForUpdate(
                                productId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found for product id: "
                                                + productId
                                )
                        );

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity to decrease must be positive"
            );
        }

        int available =
                inventory.getQuantityAvailable();

        if (available < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product id "
                            + productId
            );
        }

        inventory.setQuantityAvailable(
                available - quantity
        );
    }

    @Transactional
    public void createInventory(
            Product product
    ) {

        Inventory inventory =
                new Inventory(
                        product,
                        0
                );

        inventoryRepository.save(
                inventory
        );
    }

    private Inventory findInventory(
            Long productId
    ) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product id: "
                                        + productId
                        )
                );
    }

    private InventoryResponse toResponse(
            Inventory inventory
    ) {

        Product product =
                inventory.getProduct();

        return new InventoryResponse(
                product.getId(),
                product.getName(),
                inventory.getQuantityAvailable(),
                inventory.getVersion(),
                inventory.getUpdatedAt()
        );
    }
}