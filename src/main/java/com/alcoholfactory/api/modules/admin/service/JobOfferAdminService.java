package com.alcoholfactory.api.modules.admin.service;

import com.alcoholfactory.api.common.domain.JobOfferStatus;
import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.admin.domain.JobOffer;
import com.alcoholfactory.api.modules.admin.dto.JobOfferRequest;
import com.alcoholfactory.api.modules.admin.dto.JobOfferResponse;
import com.alcoholfactory.api.modules.admin.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOfferAdminService {

    private final JobOfferRepository jobOfferRepository;

    @Transactional(readOnly = true)
    public List<JobOfferResponse> list() {
        return jobOfferRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public JobOfferResponse create(JobOfferRequest req) {
        JobOffer j = JobOffer.builder()
                .title(req.title())
                .description(req.description())
                .status(JobOfferStatus.OPEN)
                .build();
        return toResponse(jobOfferRepository.save(j));
    }

    @Transactional
    public JobOfferResponse update(Long id, JobOfferRequest req) {
        JobOffer j = jobOfferRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        j.setTitle(req.title());
        j.setDescription(req.description());
        return toResponse(jobOfferRepository.save(j));
    }

    @Transactional
    public void delete(Long id) {
        jobOfferRepository.deleteById(id);
    }

    @Transactional
    public JobOfferResponse close(Long id) {
        JobOffer j = jobOfferRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Not found"));
        j.setStatus(JobOfferStatus.CLOSED);
        return toResponse(jobOfferRepository.save(j));
    }

    private JobOfferResponse toResponse(JobOffer j) {
        return new JobOfferResponse(j.getId(), j.getTitle(), j.getDescription(), j.getStatus(), j.getCreatedAt(), j.getUpdatedAt());
    }
}
