package com.alcoholfactory.api.modules.warehouse.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.inventory.service.InventoryService;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import com.alcoholfactory.api.modules.warehouse.domain.ReplenishmentLine;
import com.alcoholfactory.api.modules.warehouse.domain.ReplenishmentOrder;
import com.alcoholfactory.api.modules.warehouse.dto.CreateReplenishmentRequest;
import com.alcoholfactory.api.modules.warehouse.dto.ReplenishmentLineRequest;
import com.alcoholfactory.api.modules.warehouse.repository.ReplenishmentOrderRepository;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import com.alcoholfactory.api.modules.inventory.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final ReplenishmentOrderRepository replenishmentOrderRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final RawMaterialRepository rawMaterialRepository;

    @Transactional
    public ReplenishmentOrder create(Long managerId, CreateReplenishmentRequest req) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Manager not found"));
        ReplenishmentOrder order = ReplenishmentOrder.builder()
                .manager(manager)
                .note(req.note())
                .status("COMPLETED")
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
                inventoryService.patchProductStock(line.productId(), line.quantityDelta().intValue());
            } else {
                rl.setRawMaterial(rawMaterialRepository.getReferenceById(line.rawMaterialId()));
                inventoryService.patchRawMaterial(line.rawMaterialId(), line.quantityDelta());
            }
            order.getLines().add(rl);
        }
        return replenishmentOrderRepository.save(order);
    }

    private void validateLine(ReplenishmentLineRequest line) {
        boolean p = line.productId() != null;
        boolean r = line.rawMaterialId() != null;
        if (p == r) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Each line needs exactly one of productId or rawMaterialId");
        }
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentOrder> history() {
        return replenishmentOrderRepository.findAllByOrderByCreatedAtDesc();
    }
}
