package com.repoinsight.worker.analysis;

import java.nio.file.Path;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterPython;

@Component
public class PythonSourceCodeParser extends TreeSitterSourceCodeParser {

	public PythonSourceCodeParser() {
		super("Python", Set.of("py"), Set.of("class_definition"), Set.of(), Set.of(),
				Set.of("function_definition", "lambda"), Set.of("import_statement", "import_from_statement"),
				Set.of("if_statement", "for_statement", "while_statement", "try_statement", "with_statement",
						"match_statement", "conditional_expression", "except_clause"),
				Set.of("if_statement", "for_statement", "while_statement", "conditional_expression", "except_clause"),
				Set.of("case_clause"), Set.of("boolean_operator"), true);
	}

	@Override
	protected TSLanguage language(Path sourceFile) {
		return new TreeSitterPython();
	}
}