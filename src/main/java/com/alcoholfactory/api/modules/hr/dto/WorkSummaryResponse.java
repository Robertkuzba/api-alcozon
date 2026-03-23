package com.alcoholfactory.api.modules.hr.dto;

import java.util.Map;

public record WorkSummaryResponse(
        Map<Long, Long> employeeIdToWorkedMinutes
) {}
