package com.alcoholfactory.api.modules.hr.api;

import com.alcoholfactory.api.modules.hr.dto.WorkLogResponse;
import com.alcoholfactory.api.modules.hr.dto.WorkSummaryResponse;
import com.alcoholfactory.api.modules.hr.service.WorkLogService;
import com.alcoholfactory.api.security.AppUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/work-log")
@RequiredArgsConstructor
@Tag(name = "Work log")
public class WorkLogController {

    private final WorkLogService workLogService;

    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public WorkLogResponse clockIn(
            @AuthenticationPrincipal AppUserDetails user,
            @RequestParam(required = false) String notes
    ) {
        return workLogService.clockIn(user.getId(), notes);
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public WorkLogResponse clockOut(@AuthenticationPrincipal AppUserDetails user) {
        return workLogService.clockOut(user.getId());
    }

    @PostMapping("/break/start")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public WorkLogResponse breakStart(@AuthenticationPrincipal AppUserDetails user) {
        return workLogService.breakStart(user.getId());
    }

    @PostMapping("/break/end")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public WorkLogResponse breakEnd(@AuthenticationPrincipal AppUserDetails user) {
        return workLogService.breakEnd(user.getId());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<WorkLogResponse> my(
            @AuthenticationPrincipal AppUserDetails user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return workLogService.my(user.getId(), from, to);
    }

    @GetMapping("/reports/summary")
    @PreAuthorize("hasRole('MANAGER')")
    public WorkSummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return workLogService.summary(from, to);
    }
}
