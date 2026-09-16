ALTER TABLE analysis_jobs ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE analysis_jobs ADD COLUMN progress_percentage INTEGER NOT NULL DEFAULT 0;
ALTER TABLE analysis_jobs ADD COLUMN worker_id VARCHAR(255);
ALTER TABLE analysis_jobs ADD COLUMN heartbeat_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE analysis_jobs RENAME COLUMN failure_message TO failure_reason;

ALTER TABLE analysis_jobs ADD CONSTRAINT chk_analysis_job_progress
    CHECK (progress_percentage BETWEEN 0 AND 100);

CREATE INDEX idx_analysis_job_status_requested
    ON analysis_jobs(status, requested_at);