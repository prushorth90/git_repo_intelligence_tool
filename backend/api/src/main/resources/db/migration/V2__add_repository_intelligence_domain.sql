CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE github_connections (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users(id),
    github_user_id BIGINT NOT NULL UNIQUE,
    github_login VARCHAR(255) NOT NULL,
    encrypted_access_token VARCHAR(2048) NOT NULL,
    scopes VARCHAR(1024) NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE connected_repositories ADD COLUMN github_repository_id BIGINT;
ALTER TABLE connected_repositories ADD COLUMN default_branch VARCHAR(255) NOT NULL DEFAULT 'main';
ALTER TABLE connected_repositories ADD COLUMN is_private BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE connected_repositories ADD COLUMN github_connection_id UUID REFERENCES github_connections(id);

CREATE TABLE repository_analyses (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES connected_repositories(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    health_score DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    analyzed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE commit_records (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES connected_repositories(id) ON DELETE CASCADE,
    sha VARCHAR(64) NOT NULL,
    author_login VARCHAR(255),
    commit_message TEXT,
    authored_at TIMESTAMP WITH TIME ZONE NOT NULL,
    additions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0,
    files_changed INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_commit_repository_sha UNIQUE (repository_id, sha)
);

CREATE TABLE pull_request_records (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES connected_repositories(id) ON DELETE CASCADE,
    github_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    author_login VARCHAR(255),
    state VARCHAR(32) NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    merged_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_pull_request_repository_number UNIQUE (repository_id, github_number)
);

CREATE TABLE file_metrics (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES repository_analyses(id) ON DELETE CASCADE,
    file_path VARCHAR(2048) NOT NULL,
    language VARCHAR(255),
    commit_count INTEGER NOT NULL DEFAULT 0,
    additions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0,
    complexity DOUBLE PRECISION,
    risk_score DOUBLE PRECISION,
    CONSTRAINT uk_file_metric_analysis_path UNIQUE (analysis_id, file_path)
);

CREATE TABLE contributor_metrics (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES repository_analyses(id) ON DELETE CASCADE,
    github_login VARCHAR(255) NOT NULL,
    commit_count INTEGER NOT NULL DEFAULT 0,
    additions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0,
    ownership_percent DOUBLE PRECISION,
    CONSTRAINT uk_contributor_metric_analysis_login UNIQUE (analysis_id, github_login)
);

CREATE TABLE analysis_jobs (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES connected_repositories(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    failure_message TEXT
);

CREATE INDEX idx_analysis_repository ON repository_analyses(repository_id);
CREATE INDEX idx_commit_repository_authored ON commit_records(repository_id, authored_at);
CREATE INDEX idx_pull_request_repository_opened ON pull_request_records(repository_id, opened_at);
CREATE INDEX idx_analysis_job_repository_status ON analysis_jobs(repository_id, status);