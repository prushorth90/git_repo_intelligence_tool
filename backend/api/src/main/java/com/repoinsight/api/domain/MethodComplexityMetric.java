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
@Table(name = "method_complexity_metrics", uniqueConstraints = @UniqueConstraint(
		columnNames = { "analysis_id", "file_path", "symbol_name", "start_line" }))
public class MethodComplexityMetric {

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

	@Column(name = "symbol_name", nullable = false)
	private String symbolName;

	@Column(name = "symbol_kind", nullable = false)
	private String symbolKind;

	@Column(name = "start_line", nullable = false)
	private int startLine;

	@Column(name = "end_line", nullable = false)
	private int endLine;

	@Column(name = "line_count", nullable = false)
	private int lineCount;

	@Column(name = "nesting_depth", nullable = false)
	private int nestingDepth;

	@Column(name = "cyclomatic_complexity", nullable = false)
	private int cyclomaticComplexity;

	protected MethodComplexityMetric() {
	}

	public UUID getId() { return id; }
	public String getFilePath() { return filePath; }
	public String getLanguage() { return language; }
	public String getSymbolName() { return symbolName; }
	public String getSymbolKind() { return symbolKind; }
	public int getStartLine() { return startLine; }
	public int getEndLine() { return endLine; }
	public int getLineCount() { return lineCount; }
	public int getNestingDepth() { return nestingDepth; }
	public int getCyclomaticComplexity() { return cyclomaticComplexity; }
}