CREATE TABLE work_logs (
    id               BIGSERIAL PRIMARY KEY,
    employee_id      BIGINT NOT NULL REFERENCES users (id),
    clock_in_at      TIMESTAMPTZ NOT NULL,
    clock_out_at     TIMESTAMPTZ,
    break_started_at TIMESTAMPTZ,
    break_ended_at   TIMESTAMPTZ,
    notes            TEXT
);

CREATE INDEX idx_work_logs_employee ON work_logs (employee_id);
CREATE INDEX idx_work_logs_clock_in ON work_logs (clock_in_at);
