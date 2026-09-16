CREATE TABLE connected_repositories (
    id UUID PRIMARY KEY,
    owner VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(511) NOT NULL UNIQUE,
    github_url VARCHAR(1024) NOT NULL,
    connected_at TIMESTAMP WITH TIME ZONE NOT NULL
);