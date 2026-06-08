package com.alcoholfactory.api.modules.inventory.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.domain.RawMaterial;
import com.alcoholfactory.api.modules.inventory.dto.InventoryOverviewResponse;
import com.alcoholfactory.api.modules.inventory.dto.InventoryProductRow;
import com.alcoholfactory.api.modules.inventory.dto.InventoryRawRow;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.inventory.repository.RawMaterialRepository;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final ProductStockRepository productStockRepository;
  private final RawMaterialRepository rawMaterialRepository;
  private final ProductRepository productRepository;

  @Transactional(readOnly = true)
  public InventoryOverviewResponse overview() {
    var products =
        productStockRepository.findAllWithProduct().stream()
            .map(
                ps ->
                    new InventoryProductRow(
                        ps.getProduct().getId(),
                        ps.getProduct().getName(),
                        ps.getQuantity(),
                        ps.getWarehouseZone()))
            .toList();
    var raw =
        rawMaterialRepository.findAll().stream()
            .map(r -> new InventoryRawRow(r.getId(), r.getName(), r.getUnit(), r.getQuantity()))
            .toList();
    return new InventoryOverviewResponse(products, raw);
  }

  @Transactional
  public InventoryProductRow patchProductStock(Long productId, int delta) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
    ProductStock ps =
        productStockRepository
            .findById(productId)
            .orElseGet(
                () ->
                    productStockRepository.save(
                        ProductStock.builder().product(product).quantity(0).build()));
    int next = ps.getQuantity() + delta;
    if (next < 0) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Quantity would be negative");
    }
    ps.setQuantity(next);
    productStockRepository.save(ps);
    return new InventoryProductRow(
        ps.getProduct().getId(),
        ps.getProduct().getName(),
        ps.getQuantity(),
        ps.getWarehouseZone());
  }

  @Transactional
  public InventoryRawRow patchRawMaterial(Long id, BigDecimal delta) {
    RawMaterial r =
        rawMaterialRepository
            .findById(id)
            .orElseThrow(
                () -> new BusinessException(HttpStatus.NOT_FOUND, "Raw material not found"));
    BigDecimal next = r.getQuantity().add(delta);
    if (next.compareTo(BigDecimal.ZERO) < 0) {
      throw new BusinessException(HttpStatus.BAD_REQUEST, "Quantity would be negative");
    }
    r.setQuantity(next);
    rawMaterialRepository.save(r);
    return new InventoryRawRow(r.getId(), r.getName(), r.getUnit(), r.getQuantity());
  }
}
