package com.alcoholfactory.api.modules.admin.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.admin.domain.DeliveryAnnouncement;
import com.alcoholfactory.api.modules.admin.dto.AnnouncementRequest;
import com.alcoholfactory.api.modules.admin.dto.AnnouncementResponse;
import com.alcoholfactory.api.modules.admin.repository.DeliveryAnnouncementRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryAnnouncementService {

    private final DeliveryAnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> list() {
        return announcementRepository.findAllByOrderByPublishedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AnnouncementResponse create(Long managerId, AnnouncementRequest req) {
        User creator = userRepository.findById(managerId).orElseThrow();
        DeliveryAnnouncement a = DeliveryAnnouncement.builder()
                .title(req.title())
                .content(req.content())
                .publishedAt(Instant.now())
                .createdBy(creator)
                .createdAt(Instant.now())
                .build();
        return toResponse(announcementRepository.save(a));
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest req) {
        DeliveryAnnouncement a = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        a.setTitle(req.title());
        a.setContent(req.content());
        return toResponse(announcementRepository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        announcementRepository.deleteById(id);
    }

    private AnnouncementResponse toResponse(DeliveryAnnouncement a) {
        return new AnnouncementResponse(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getPublishedAt(),
                a.getCreatedBy().getId(),
                a.getCreatedAt()
        );
    }
}
