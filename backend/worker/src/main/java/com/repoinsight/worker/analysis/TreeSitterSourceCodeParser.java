package com.repoinsight.worker.analysis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;

public abstract class TreeSitterSourceCodeParser implements SourceCodeParser {

	private static final int MAX_SOURCE_BYTES = 10 * 1024 * 1024;
	private static final int MAX_METADATA_ITEMS = 10_000;

	private final String languageName;
	private final Set<String> extensions;
	private final Set<String> classNodes;
	private final Set<String> interfaceNodes;
	private final Set<String> methodNodes;
	private final Set<String> functionNodes;
	private final Set<String> importNodes;
	private final Set<String> controlFlowNodes;
	private final boolean nestedFunctionsAreMethods;

	protected TreeSitterSourceCodeParser(
			String languageName,
			Set<String> extensions,
			Set<String> classNodes,
			Set<String> interfaceNodes,
			Set<String> methodNodes,
			Set<String> functionNodes,
			Set<String> importNodes,
			Set<String> controlFlowNodes,
			boolean nestedFunctionsAreMethods) {
		this.languageName = languageName;
		this.extensions = extensions;
		this.classNodes = classNodes;
		this.interfaceNodes = interfaceNodes;
		this.methodNodes = methodNodes;
		this.functionNodes = functionNodes;
		this.importNodes = importNodes;
		this.controlFlowNodes = controlFlowNodes;
		this.nestedFunctionsAreMethods = nestedFunctionsAreMethods;
	}

	protected abstract TSLanguage language(Path sourceFile);

	@Override
	public boolean supports(Path sourceFile) {
		String name = sourceFile.getFileName().toString().toLowerCase(Locale.ROOT);
		int separator = name.lastIndexOf('.');
		return separator >= 0 && extensions.contains(name.substring(separator + 1));
	}

	@Override
	public StructuralFileMetrics parse(Path repositoryRoot, Path sourceFile) {
		try {
			byte[] sourceBytes = Files.readAllBytes(sourceFile);
			if (sourceBytes.length > MAX_SOURCE_BYTES) {
				throw new IllegalArgumentException("Source file exceeds the structural parsing size limit.");
			}
			String source = new String(sourceBytes, StandardCharsets.UTF_8);
			try (TSLanguage grammar = language(sourceFile); TSParser parser = new TSParser()) {
				if (!parser.setLanguage(grammar)) throw new IllegalStateException("Tree-sitter rejected the " + languageName + " grammar.");
				try (TSTree tree = parser.parseString(null, source)) {
					Accumulator accumulator = new Accumulator(repositoryRoot, sourceFile, sourceBytes, tree.getRootNode());
					accumulator.walk(tree.getRootNode(), 0, false);
					return accumulator.result();
				}
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read source file for structural analysis: " + sourceFile, exception);
		}
	}

	private final class Accumulator {
		private final String relativePath;
		private final byte[] source;
		private final boolean parseError;
		private final List<String> imports = new ArrayList<>();
		private final List<StructuralSymbol> symbols = new ArrayList<>();
		private int classes;
		private int interfaces;
		private int methods;
		private int functions;
		private int controlFlow;
		private int maximumNesting;
		private int methodLengthTotal;
		private int methodLengthMaximum;

		private Accumulator(Path root, Path sourceFile, byte[] source, TSNode rootNode) {
			this.relativePath = root.relativize(sourceFile).toString().replace('\\', '/');
			this.source = source;
			this.parseError = rootNode.hasError();
		}

		private void walk(TSNode node, int controlDepth, boolean insideType) {
			String type = node.getType();
			boolean typeDeclaration = classNodes.contains(type) || interfaceNodes.contains(type);
			boolean nextInsideType = insideType || typeDeclaration;
			if (classNodes.contains(type)) {
				classes++;
				addSymbol(node, "class", 0);
			}
			if (interfaceNodes.contains(type)) {
				interfaces++;
				addSymbol(node, "interface", 0);
			}
			if (methodNodes.contains(type) || (nestedFunctionsAreMethods && insideType && functionNodes.contains(type))) {
				methods++;
				addCallable(node, "method");
			} else if (functionNodes.contains(type)) {
				functions++;
				addCallable(node, "function");
			}
			if (importNodes.contains(type) && imports.size() < MAX_METADATA_ITEMS) {
				imports.add(text(node).replaceAll("\\s+", " ").trim());
			}

			int nextDepth = controlDepth;
			if (controlFlowNodes.contains(type)) {
				controlFlow++;
				nextDepth++;
				maximumNesting = Math.max(maximumNesting, nextDepth);
			}
			for (int index = 0; index < node.getNamedChildCount(); index++) {
				walk(node.getNamedChild(index), nextDepth, nextInsideType);
			}
		}

		private void addCallable(TSNode node, String kind) {
			int length = lineLength(node);
			methodLengthTotal += length;
			methodLengthMaximum = Math.max(methodLengthMaximum, length);
			addSymbol(node, kind, maxControlDepth(node, 0));
		}

		private void addSymbol(TSNode node, String kind, int nestingDepth) {
			if (symbols.size() >= MAX_METADATA_ITEMS) return;
			symbols.add(new StructuralSymbol(
					symbolName(node), kind, node.getStartPoint().getRow() + 1, node.getEndPoint().getRow() + 1,
					lineLength(node), nestingDepth));
		}

		private String symbolName(TSNode node) {
			TSNode name = node.getChildByFieldName("name");
			if ((name == null || name.isNull()) && node.getParent() != null) {
				name = node.getParent().getChildByFieldName("name");
			}
			return name == null || name.isNull() ? "[anonymous]" : text(name).trim();
		}

		private int maxControlDepth(TSNode node, int depth) {
			int current = controlFlowNodes.contains(node.getType()) ? depth + 1 : depth;
			int maximum = current;
			for (int index = 0; index < node.getNamedChildCount(); index++) {
				maximum = Math.max(maximum, maxControlDepth(node.getNamedChild(index), current));
			}
			return maximum;
		}

		private int lineLength(TSNode node) {
			return node.getEndPoint().getRow() - node.getStartPoint().getRow() + 1;
		}

		private String text(TSNode node) {
			int start = Math.max(0, Math.min(node.getStartByte(), source.length));
			int end = Math.max(start, Math.min(node.getEndByte(), source.length));
			return new String(source, start, end - start, StandardCharsets.UTF_8);
		}

		private StructuralFileMetrics result() {
			int callableCount = methods + functions;
			return new StructuralFileMetrics(relativePath, languageName, classes, interfaces, methods, functions,
					List.copyOf(imports), callableCount == 0 ? 0 : (double) methodLengthTotal / callableCount,
					methodLengthMaximum, maximumNesting, controlFlow, parseError, List.copyOf(symbols));
		}
	}
}