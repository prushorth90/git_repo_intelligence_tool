ALTER TABLE file_metrics DROP CONSTRAINT uk_file_metric_analysis_path;
ALTER TABLE file_metrics ADD COLUMN period VARCHAR(32) NOT NULL DEFAULT 'ALL';
ALTER TABLE file_metrics ADD COLUMN contributor_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE file_metrics ADD COLUMN last_modified_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE file_metrics ADD COLUMN total_churn BIGINT NOT NULL DEFAULT 0;

ALTER TABLE file_metrics
    ADD CONSTRAINT uk_file_metric_analysis_path_period
    UNIQUE (analysis_id, file_path, period);

CREATE INDEX idx_file_metric_analysis_period_churn
    ON file_metrics(analysis_id, period, total_churn DESC);