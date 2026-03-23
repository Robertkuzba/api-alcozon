package com.alcoholfactory.api.modules.admin.repository;

import com.alcoholfactory.api.modules.admin.domain.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
}
