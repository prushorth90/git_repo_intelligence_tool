ALTER TABLE github_connections ADD COLUMN avatar_url VARCHAR(2048) NOT NULL DEFAULT '';

ALTER TABLE connected_repositories DROP CONSTRAINT IF EXISTS connected_repositories_github_connection_id_fkey;
ALTER TABLE connected_repositories
	ADD CONSTRAINT connected_repositories_github_connection_id_fkey
	FOREIGN KEY (github_connection_id) REFERENCES github_connections(id) ON DELETE SET NULL;