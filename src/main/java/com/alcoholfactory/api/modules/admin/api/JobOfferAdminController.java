package com.alcoholfactory.api.modules.admin.api;

import com.alcoholfactory.api.modules.admin.dto.JobOfferRequest;
import com.alcoholfactory.api.modules.admin.dto.JobOfferResponse;
import com.alcoholfactory.api.modules.admin.service.JobOfferAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/job-offers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Job offers")
public class JobOfferAdminController {

  private final JobOfferAdminService jobOfferAdminService;

  @GetMapping
  public List<JobOfferResponse> list() {
    return jobOfferAdminService.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public JobOfferResponse create(@Valid @RequestBody JobOfferRequest req) {
    return jobOfferAdminService.create(req);
  }

  @PutMapping("/{id}")
  public JobOfferResponse update(@PathVariable Long id, @Valid @RequestBody JobOfferRequest req) {
    return jobOfferAdminService.update(id, req);
  }

  @PostMapping("/{id}/close")
  public JobOfferResponse close(@PathVariable Long id) {
    return jobOfferAdminService.close(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    jobOfferAdminService.delete(id);
  }
}
