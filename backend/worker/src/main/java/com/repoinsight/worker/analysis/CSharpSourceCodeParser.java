package com.repoinsight.worker.analysis;

import java.nio.file.Path;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterCSharp;

@Component
public class CSharpSourceCodeParser extends TreeSitterSourceCodeParser {

	public CSharpSourceCodeParser() {
		super("C#", Set.of("cs"),
				Set.of("class_declaration", "record_declaration", "struct_declaration", "enum_declaration"),
				Set.of("interface_declaration"),
				Set.of("method_declaration", "constructor_declaration", "destructor_declaration", "operator_declaration"),
				Set.of("local_function_statement"), Set.of("using_directive"),
				Set.of("if_statement", "for_statement", "for_each_statement", "while_statement", "do_statement",
						"switch_statement", "switch_expression", "catch_clause", "conditional_expression"),
				Set.of("if_statement", "for_statement", "for_each_statement", "while_statement", "do_statement",
						"catch_clause", "conditional_expression"),
				Set.of("case_switch_label", "switch_expression_arm"), Set.of("binary_expression"), false);
	}

	@Override
	protected TSLanguage language(Path sourceFile) {
		return new TreeSitterCSharp();
	}
}