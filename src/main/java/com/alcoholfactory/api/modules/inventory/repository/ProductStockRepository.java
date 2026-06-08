package com.alcoholfactory.api.modules.inventory.repository;

import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

  @Query("SELECT ps FROM ProductStock ps JOIN FETCH ps.product")
  List<ProductStock> findAllWithProduct();
}
