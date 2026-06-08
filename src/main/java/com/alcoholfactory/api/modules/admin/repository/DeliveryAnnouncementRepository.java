package com.alcoholfactory.api.modules.admin.repository;

import com.alcoholfactory.api.modules.admin.domain.DeliveryAnnouncement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAnnouncementRepository extends JpaRepository<DeliveryAnnouncement, Long> {

  List<DeliveryAnnouncement> findAllByOrderByPublishedAtDesc();
}
