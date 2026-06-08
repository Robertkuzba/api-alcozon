package com.alcoholfactory.api.modules.hr.domain;

import com.alcoholfactory.api.modules.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private User employee;

  @Column(name = "clock_in_at", nullable = false)
  private Instant clockInAt;

  @Column(name = "clock_out_at")
  private Instant clockOutAt;

  @Column(name = "break_started_at")
  private Instant breakStartedAt;

  @Column(name = "break_ended_at")
  private Instant breakEndedAt;

  @Column(columnDefinition = "TEXT")
  private String notes;
}
