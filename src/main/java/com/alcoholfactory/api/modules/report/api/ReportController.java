package com.alcoholfactory.api.modules.report.api;

import com.alcoholfactory.api.modules.hr.dto.WorkSummaryResponse;
import com.alcoholfactory.api.modules.hr.service.WorkLogService;
import com.alcoholfactory.api.modules.inventory.dto.InventoryOverviewResponse;
import com.alcoholfactory.api.modules.report.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Reports")
public class ReportController {

    private final ReportService reportService;
    private final WorkLogService workLogService;

    @GetMapping("/sales")
    public Map<String, BigDecimal> sales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return Map.of("totalAmount", reportService.salesTotal(from, to));
    }

    @GetMapping("/inventory")
    public InventoryOverviewResponse inventory() {
        return reportService.inventorySnapshot();
    }

    @GetMapping("/employees/work-summary")
    public WorkSummaryResponse workSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return workLogService.summary(from, to);
    }
}
