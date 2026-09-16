package com.repoinsight.api.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectedRepositoryRepository extends JpaRepository<ConnectedRepository, UUID> {

	boolean existsByFullNameIgnoreCase(String fullName);
}