package com.alcoholfactory.api.modules.hr.repository;

import com.alcoholfactory.api.modules.hr.domain.WorkLog;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {

  List<WorkLog> findByEmployeeIdAndClockInAtBetweenOrderByClockInAtDesc(
      Long employeeId, Instant from, Instant to);

  @Query(
      "SELECT w FROM WorkLog w WHERE w.employee.id = :empId AND w.clockOutAt IS NULL ORDER BY"
          + " w.clockInAt DESC")
  List<WorkLog> findOpenSessions(@Param("empId") Long employeeId);

  @Query("SELECT w FROM WorkLog w WHERE w.employee.id = :empId AND w.clockOutAt IS NULL")
  Optional<WorkLog> findFirstOpenSession(@Param("empId") Long employeeId);

  @Query(
      "SELECT w FROM WorkLog w JOIN FETCH w.employee WHERE w.clockInAt >= :from AND w.clockInAt <="
          + " :to AND w.clockOutAt IS NOT NULL")
  List<WorkLog> findCompletedInRange(@Param("from") Instant from, @Param("to") Instant to);
}
