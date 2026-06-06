package com.alcoholfactory.api.modules.warehouse.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.inventory.service.InventoryService;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.modules.warehouse.domain.ReplenishmentLine;
import com.alcoholfactory.api.modules.warehouse.domain.ReplenishmentOrder;
import com.alcoholfactory.api.modules.warehouse.dto.CreateReplenishmentRequest;
import com.alcoholfactory.api.modules.warehouse.dto.PatchReplenishmentStatusRequest;
import com.alcoholfactory.api.modules.warehouse.dto.ReplenishmentLineRequest;
import com.alcoholfactory.api.modules.warehouse.dto.ReplenishmentLineResponse;
import com.alcoholfactory.api.modules.warehouse.dto.ReplenishmentOrderResponse;
import com.alcoholfactory.api.modules.warehouse.repository.ReplenishmentOrderRepository;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.inventory.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private static final String STATUS_PENDING = "PENDING";
    private static final Set<String> FULFILLED_STATUSES = Set.of("RECEIVED", "COMPLETED");

    private final ReplenishmentOrderRepository replenishmentOrderRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;

    @Transactional
    public ReplenishmentOrderResponse create(Long managerId, CreateReplenishmentRequest req) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Manager not found"));
        ReplenishmentOrder order = ReplenishmentOrder.builder()
                .manager(manager)
                .note(req.note())
                .status(STATUS_PENDING)
                .createdAt(Instant.now())
                .build();
        for (ReplenishmentLineRequest line : req.lines()) {
            validateLine(line);
            ReplenishmentLine rl = ReplenishmentLine.builder()
                    .replenishment(order)
                    .quantityDelta(line.quantityDelta())
                    .build();
            if (line.productId() != null) {
                rl.setProduct(productRepository.getReferenceById(line.productId()));
            } else {
                rl.setRawMaterial(rawMaterialRepository.getReferenceById(line.rawMaterialId()));
            }
            order.getLines().add(rl);
        }
        ReplenishmentOrder saved = replenishmentOrderRepository.save(order);
        return replenishmentOrderRepository.findWithDetailsById(saved.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Replenishment order not found after save"));
    }

    private void validateLine(ReplenishmentLineRequest line) {
        boolean p = line.productId() != null;
        boolean r = line.rawMaterialId() != null;
        if (p == r) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Each line needs exactly one of productId or rawMaterialId");
        }
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentOrderResponse> history() {
        return replenishmentOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReplenishmentOrderResponse applyStatus(Long orderId, PatchReplenishmentStatusRequest req) {
        String targetStatus = req.status().trim().toUpperCase();
        if (!FULFILLED_STATUSES.contains(targetStatus)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status must be RECEIVED or COMPLETED");
        }
        ReplenishmentOrder order = replenishmentOrderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Replenishment order not found"));
        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Replenishment order is already fulfilled");
        }
        applyLinesToInventory(order);
        order.setStatus(targetStatus);
        replenishmentOrderRepository.save(order);
        return toResponse(order);
    }

    private void applyLinesToInventory(ReplenishmentOrder order) {
        for (ReplenishmentLine line : order.getLines()) {
            if (line.getProduct() != null) {
                inventoryService.patchProductStock(
                        line.getProduct().getId(),
                        line.getQuantityDelta().intValue());
            } else if (line.getRawMaterial() != null) {
                inventoryService.patchRawMaterial(
                        line.getRawMaterial().getId(),
                        line.getQuantityDelta());
            }
        }
    }

    private ReplenishmentOrderResponse toResponse(ReplenishmentOrder order) {
        List<ReplenishmentLineResponse> lineDtos = order.getLines().stream()
                .map(this::toLineResponse)
                .toList();
        User manager = order.getManager();
        return new ReplenishmentOrderResponse(
                order.getId(),
                manager.getId(),
                manager.getEmail(),
                order.getNote(),
                order.getStatus(),
                order.getCreatedAt(),
                lineDtos
        );
    }

    private ReplenishmentLineResponse toLineResponse(ReplenishmentLine line) {
        Long productId = null;
        String productName = null;
        Long rawMaterialId = null;
        String rawMaterialName = null;
        if (line.getProduct() != null) {
            productId = line.getProduct().getId();
            productName = line.getProduct().getName();
        }
        if (line.getRawMaterial() != null) {
            rawMaterialId = line.getRawMaterial().getId();
            rawMaterialName = line.getRawMaterial().getName();
        }
        return new ReplenishmentLineResponse(
                line.getId(),
                productId,
                productName,
                rawMaterialId,
                rawMaterialName,
                line.getQuantityDelta()
        );
    }
}
