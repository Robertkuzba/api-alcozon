package com.alcoholfactory.api.modules.product.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.inventory.domain.ProductStock;
import com.alcoholfactory.api.modules.inventory.repository.ProductStockRepository;
import com.alcoholfactory.api.modules.product.domain.Product;
import com.alcoholfactory.api.modules.product.dto.CreateProductRequest;
import com.alcoholfactory.api.modules.product.dto.ProductResponse;
import com.alcoholfactory.api.modules.product.dto.UpdateProductRequest;
import com.alcoholfactory.api.modules.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductStockRepository productStockRepository;

  @Transactional(readOnly = true)
  public Page<ProductResponse> search(
      String q, String category, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
    Specification<Product> spec =
        (root, query, cb) -> {
          List<Predicate> p = new ArrayList<>();
          p.add(cb.isTrue(root.get("active")));
          if (StringUtils.hasText(q)) {
            String pattern = "%" + q.toLowerCase() + "%";
            p.add(
                cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)));
          }
          if (StringUtils.hasText(category)) {
            p.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
          }
          if (minPrice != null) {
            p.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
          }
          if (maxPrice != null) {
            p.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
          }
          return cb.and(p.toArray(Predicate[]::new));
        };
    return productRepository.findAll(spec, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public ProductResponse getById(Long id) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
    if (!p.isActive()) {
      throw new BusinessException(HttpStatus.NOT_FOUND, "Product not found");
    }
    return toResponse(p);
  }

  @Transactional(readOnly = true)
  public ProductResponse getByIdForManager(Long id) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
    return toResponse(p);
  }

  @Transactional
  public ProductResponse create(CreateProductRequest req) {
    Product p =
        Product.builder()
            .name(req.name())
            .description(req.description())
            .category(req.category())
            .price(req.price())
            .volumeMl(req.volumeMl())
            .abv(req.abv())
            .imageUrl(req.imageUrl())
            .active(true)
            .build();
    productRepository.save(p);
    ProductStock stock =
        ProductStock.builder().product(p).quantity(Math.max(0, req.initialStock())).build();
    productStockRepository.save(stock);
    return toResponse(p);
  }

  @Transactional
  public ProductResponse update(Long id, UpdateProductRequest req) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
    p.setName(req.name());
    p.setDescription(req.description());
    p.setCategory(req.category());
    p.setPrice(req.price());
    p.setVolumeMl(req.volumeMl());
    p.setAbv(req.abv());
    p.setImageUrl(req.imageUrl());
    p.setActive(req.active());
    return toResponse(p);
  }

  @Transactional
  public void deactivate(Long id) {
    Product p =
        productRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Product not found"));
    p.setActive(false);
  }

  private ProductResponse toResponse(Product p) {
    int stock = productStockRepository.findById(p.getId()).map(ProductStock::getQuantity).orElse(0);
    return new ProductResponse(
        p.getId(),
        p.getName(),
        p.getDescription(),
        p.getCategory(),
        p.getPrice(),
        p.getVolumeMl(),
        p.getAbv(),
        p.getImageUrl(),
        p.isActive(),
        stock);
  }
}
