package com.alcoholfactory.api.modules.hr.service;

import com.alcoholfactory.api.common.error.BusinessException;
import com.alcoholfactory.api.modules.hr.domain.WorkLog;
import com.alcoholfactory.api.modules.hr.dto.WorkLogResponse;
import com.alcoholfactory.api.modules.hr.dto.WorkSummaryResponse;
import com.alcoholfactory.api.modules.hr.repository.WorkLogRepository;
import com.alcoholfactory.api.modules.user.domain.User;
import com.alcoholfactory.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkLogResponse clockIn(Long employeeId, String notes) {
        workLogRepository.findFirstOpenSession(employeeId).ifPresent(w -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Already clocked in");
        });
        User emp = userRepository.findById(employeeId).orElseThrow();
        WorkLog w = WorkLog.builder()
                .employee(emp)
                .clockInAt(Instant.now())
                .notes(notes)
                .build();
        workLogRepository.save(w);
        return toResponse(w);
    }

    @Transactional
    public WorkLogResponse clockOut(Long employeeId) {
        WorkLog w = workLogRepository.findFirstOpenSession(employeeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "No open session"));
        if (w.getBreakStartedAt() != null && w.getBreakEndedAt() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "End break first");
        }
        w.setClockOutAt(Instant.now());
        workLogRepository.save(w);
        return toResponse(w);
    }

    @Transactional
    public WorkLogResponse breakStart(Long employeeId) {
        WorkLog w = workLogRepository.findFirstOpenSession(employeeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "No open session"));
        if (w.getBreakStartedAt() != null && w.getBreakEndedAt() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Break already active");
        }
        w.setBreakStartedAt(Instant.now());
        w.setBreakEndedAt(null);
        workLogRepository.save(w);
        return toResponse(w);
    }

    @Transactional
    public WorkLogResponse breakEnd(Long employeeId) {
        WorkLog w = workLogRepository.findFirstOpenSession(employeeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "No open session"));
        if (w.getBreakStartedAt() == null || w.getBreakEndedAt() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No active break");
        }
        w.setBreakEndedAt(Instant.now());
        workLogRepository.save(w);
        return toResponse(w);
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponse> my(Long employeeId, Instant from, Instant to) {
        return workLogRepository.findByEmployeeIdAndClockInAtBetweenOrderByClockInAtDesc(employeeId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkSummaryResponse summary(Instant from, Instant to) {
        List<WorkLog> logs = workLogRepository.findCompletedInRange(from, to);
        Map<Long, Long> map = new HashMap<>();
        for (WorkLog w : logs) {
            long minutes = Duration.between(w.getClockInAt(), w.getClockOutAt()).toMinutes();
            map.merge(w.getEmployee().getId(), minutes, Long::sum);
        }
        return new WorkSummaryResponse(map);
    }

    private WorkLogResponse toResponse(WorkLog w) {
        return new WorkLogResponse(
                w.getId(),
                w.getClockInAt(),
                w.getClockOutAt(),
                w.getBreakStartedAt(),
                w.getBreakEndedAt(),
                w.getNotes()
        );
    }
}
