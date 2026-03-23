package com.alcoholfactory.api.modules.admin.repository;

import com.alcoholfactory.api.modules.admin.domain.DeliveryAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAnnouncementRepository extends JpaRepository<DeliveryAnnouncement, Long> {

    List<DeliveryAnnouncement> findAllByOrderByPublishedAtDesc();
}
