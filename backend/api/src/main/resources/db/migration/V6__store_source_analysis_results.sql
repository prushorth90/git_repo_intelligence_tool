ALTER TABLE analysis_jobs ADD COLUMN include_history BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE repository_analyses ADD COLUMN analysis_job_id UUID;
ALTER TABLE repository_analyses ADD COLUMN source_file_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE repository_analyses ADD COLUMN repository_size_bytes BIGINT NOT NULL DEFAULT 0;
ALTER TABLE repository_analyses ADD COLUMN language_distribution TEXT NOT NULL DEFAULT '{}';
ALTER TABLE repository_analyses ADD COLUMN directory_structure TEXT NOT NULL DEFAULT '[]';
ALTER TABLE repository_analyses ADD COLUMN file_extensions TEXT NOT NULL DEFAULT '{}';
ALTER TABLE repository_analyses ADD COLUMN history_included BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE repository_analyses
    ADD CONSTRAINT fk_repository_analysis_job
    FOREIGN KEY (analysis_job_id) REFERENCES analysis_jobs(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX uk_repository_analysis_job
    ON repository_analyses(analysis_job_id);