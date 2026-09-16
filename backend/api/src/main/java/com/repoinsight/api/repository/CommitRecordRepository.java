package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.CommitRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitRecordRepository extends JpaRepository<CommitRecord, UUID> {
	List<CommitRecord> findByRepositoryIdOrderByAuthoredAtDesc(UUID repositoryId);
}