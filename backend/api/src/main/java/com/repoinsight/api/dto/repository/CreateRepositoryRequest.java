package com.repoinsight.api.dto.repository;

import jakarta.validation.constraints.NotBlank;

public record CreateRepositoryRequest(@NotBlank String githubUrl) {
}