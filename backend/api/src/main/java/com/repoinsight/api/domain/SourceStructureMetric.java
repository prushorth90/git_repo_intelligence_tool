package com.repoinsight.api.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "source_structure_metrics", uniqueConstraints = @UniqueConstraint(
		columnNames = { "analysis_id", "file_path" }))
public class SourceStructureMetric {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "analysis_id", nullable = false)
	private RepositoryAnalysis analysis;

	@Column(name = "file_path", nullable = false, length = 2048)
	private String filePath;

	@Column(nullable = false)
	private String language;

	@Column(name = "class_count", nullable = false)
	private int classCount;

	@Column(name = "interface_count", nullable = false)
	private int interfaceCount;

	@Column(name = "method_count", nullable = false)
	private int methodCount;

	@Column(name = "function_count", nullable = false)
	private int functionCount;

	@Column(name = "import_count", nullable = false)
	private int importCount;

	@Column(name = "average_method_length", nullable = false)
	private double averageMethodLength;

	@Column(name = "maximum_method_length", nullable = false)
	private int maximumMethodLength;

	@Column(name = "maximum_nesting_depth", nullable = false)
	private int maximumNestingDepth;

	@Column(name = "control_flow_count", nullable = false)
	private int controlFlowCount;

	@Column(name = "cyclomatic_complexity", nullable = false)
	private int cyclomaticComplexity;

	@Column(name = "maximum_method_complexity", nullable = false)
	private int maximumMethodComplexity;

	@Column(name = "parse_error", nullable = false)
	private boolean parseError;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String symbols;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String imports;

	protected SourceStructureMetric() {
	}

	public UUID getId() { return id; }
	public String getFilePath() { return filePath; }
	public String getLanguage() { return language; }
	public int getClassCount() { return classCount; }
	public int getInterfaceCount() { return interfaceCount; }
	public int getMethodCount() { return methodCount; }
	public int getFunctionCount() { return functionCount; }
	public int getImportCount() { return importCount; }
	public double getAverageMethodLength() { return averageMethodLength; }
	public int getMaximumMethodLength() { return maximumMethodLength; }
	public int getMaximumNestingDepth() { return maximumNestingDepth; }
	public int getControlFlowCount() { return controlFlowCount; }
	public int getCyclomaticComplexity() { return cyclomaticComplexity; }
	public int getMaximumMethodComplexity() { return maximumMethodComplexity; }
	public boolean isParseError() { return parseError; }
}