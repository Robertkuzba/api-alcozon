package com.alcoholfactory.api.modules.inventory.domain;

import com.alcoholfactory.api.modules.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStock {

  @Id
  @Column(name = "product_id")
  private Long id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "warehouse_zone", length = 64)
  private String warehouseZone;

  @Column(name = "last_updated_at", nullable = false)
  private Instant lastUpdatedAt;

  @PrePersist
  @PreUpdate
  void touch() {
    lastUpdatedAt = Instant.now();
  }
}
