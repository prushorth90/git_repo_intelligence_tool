CREATE TABLE file_hotspot_metrics (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES repository_analyses(id) ON DELETE CASCADE,
    file_path VARCHAR(2048) NOT NULL,
    language VARCHAR(64) NOT NULL,
    churn BIGINT NOT NULL,
    complexity INTEGER NOT NULL,
    contributor_concentration DOUBLE PRECISION NOT NULL,
    bug_fix_commits INTEGER NOT NULL,
    dependency_references INTEGER NOT NULL,
    recent_modifications INTEGER NOT NULL,
    normalized_churn DOUBLE PRECISION NOT NULL,
    normalized_complexity DOUBLE PRECISION NOT NULL,
    normalized_contributor_concentration DOUBLE PRECISION NOT NULL,
    normalized_bug_fix_activity DOUBLE PRECISION NOT NULL,
    normalized_dependency_importance DOUBLE PRECISION NOT NULL,
    normalized_modification_frequency DOUBLE PRECISION NOT NULL,
    risk_score DOUBLE PRECISION NOT NULL,
    risk_level VARCHAR(16) NOT NULL,
    CONSTRAINT uk_file_hotspot_analysis_path UNIQUE (analysis_id, file_path),
    CONSTRAINT ck_file_hotspot_risk_score CHECK (risk_score BETWEEN 0 AND 100),
    CONSTRAINT ck_file_hotspot_risk_level CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE INDEX idx_file_hotspot_analysis_risk
    ON file_hotspot_metrics(analysis_id, risk_score DESC, file_path);