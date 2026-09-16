package com.repoinsight.api.repository;

import java.util.List;
import java.util.UUID;

import com.repoinsight.api.domain.PullRequestRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PullRequestRecordRepository extends JpaRepository<PullRequestRecord, UUID> {
	List<PullRequestRecord> findByRepositoryIdOrderByOpenedAtDesc(UUID repositoryId);
}