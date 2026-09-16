ALTER TABLE connected_repositories ADD COLUMN visibility VARCHAR(32) NOT NULL DEFAULT 'public';
ALTER TABLE connected_repositories ADD COLUMN primary_language VARCHAR(255);
ALTER TABLE connected_repositories ADD COLUMN stargazers_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE connected_repositories ADD COLUMN forks_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE connected_repositories ADD COLUMN github_updated_at TIMESTAMP WITH TIME ZONE;

CREATE UNIQUE INDEX uk_connected_repository_github_id
    ON connected_repositories(github_repository_id);