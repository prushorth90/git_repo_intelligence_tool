package com.repoinsight.worker.analysis;

import java.nio.file.Path;

public interface SourceCodeParser {

	boolean supports(Path sourceFile);

	StructuralFileMetrics parse(Path repositoryRoot, Path sourceFile);
}