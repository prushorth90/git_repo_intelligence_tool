package com.repoinsight.worker.analysis;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterTsx;
import org.treesitter.TreeSitterTypescript;

@Component
public class TypeScriptSourceCodeParser extends TreeSitterSourceCodeParser {

	public TypeScriptSourceCodeParser() {
		super("TypeScript", Set.of("ts", "tsx"), Set.of("class_declaration", "abstract_class_declaration"),
				Set.of("interface_declaration"), Set.of("method_definition", "abstract_method_signature"),
				Set.of("function_declaration", "generator_function_declaration", "arrow_function"),
				Set.of("import_statement"),
				Set.of("if_statement", "for_statement", "for_in_statement", "while_statement", "do_statement",
						"switch_statement", "catch_clause", "ternary_expression"), false);
	}

	@Override
	protected TSLanguage language(Path sourceFile) {
		return sourceFile.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".tsx")
				? new TreeSitterTsx()
				: new TreeSitterTypescript();
	}
}