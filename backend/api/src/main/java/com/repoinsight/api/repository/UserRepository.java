package com.repoinsight.api.repository;

import java.util.Optional;
import java.util.UUID;

import com.repoinsight.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmailIgnoreCase(String email);
}