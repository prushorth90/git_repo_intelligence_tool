ALTER TABLE contributor_metrics RENAME COLUMN github_login TO contributor_key;
ALTER TABLE contributor_metrics ADD COLUMN display_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE contributor_metrics ADD COLUMN files_touched INTEGER NOT NULL DEFAULT 0;
ALTER TABLE contributor_metrics ADD COLUMN last_activity_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE contributor_metrics ADD COLUMN primary_modules TEXT NOT NULL DEFAULT '[]';
ALTER TABLE contributor_metrics ADD COLUMN weighted_score DOUBLE PRECISION NOT NULL DEFAULT 0;
ALTER TABLE contributor_metrics ALTER COLUMN ownership_percent SET DEFAULT 0;

ALTER TABLE file_metrics ADD COLUMN top_contributor_name VARCHAR(255);
ALTER TABLE file_metrics ADD COLUMN top_ownership_percent DOUBLE PRECISION;
ALTER TABLE file_metrics ADD COLUMN bus_factor INTEGER NOT NULL DEFAULT 0;
ALTER TABLE file_metrics ADD COLUMN concentrated_ownership BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE file_ownership_metrics (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES repository_analyses(id) ON DELETE CASCADE,
    file_path VARCHAR(2048) NOT NULL,
    contributor_key VARCHAR(512) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    commit_count INTEGER NOT NULL,
    additions INTEGER NOT NULL,
    deletions INTEGER NOT NULL,
    last_modified_at TIMESTAMP WITH TIME ZONE,
    ownership_percent DOUBLE PRECISION NOT NULL,
    CONSTRAINT uk_file_ownership_analysis_path_contributor
        UNIQUE (analysis_id, file_path, contributor_key)
);

CREATE INDEX idx_contributor_metric_analysis_ownership
    ON contributor_metrics(analysis_id, ownership_percent DESC);
CREATE INDEX idx_file_metric_concentrated
    ON file_metrics(analysis_id, concentrated_ownership, top_ownership_percent DESC);