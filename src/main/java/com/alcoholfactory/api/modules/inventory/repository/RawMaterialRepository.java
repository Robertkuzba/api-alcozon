package com.alcoholfactory.api.modules.inventory.repository;

import com.alcoholfactory.api.modules.inventory.domain.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {}
