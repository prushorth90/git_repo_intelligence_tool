package com.repoinsight.worker.analysis;

import java.nio.file.Path;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJava;

@Component
public class JavaSourceCodeParser extends TreeSitterSourceCodeParser {

	public JavaSourceCodeParser() {
		super("Java", Set.of("java"),
				Set.of("class_declaration", "record_declaration", "enum_declaration", "annotation_type_declaration"),
				Set.of("interface_declaration"),
				Set.of("method_declaration", "constructor_declaration"), Set.of(), Set.of("import_declaration"),
				Set.of("if_statement", "for_statement", "enhanced_for_statement", "while_statement", "do_statement",
						"switch_expression", "catch_clause", "ternary_expression"),
				Set.of("if_statement", "for_statement", "enhanced_for_statement", "while_statement", "do_statement",
						"catch_clause", "ternary_expression"),
				Set.of("switch_label", "switch_rule"), Set.of("binary_expression"), false);
	}

	@Override
	protected TSLanguage language(Path sourceFile) {
		return new TreeSitterJava();
	}
}