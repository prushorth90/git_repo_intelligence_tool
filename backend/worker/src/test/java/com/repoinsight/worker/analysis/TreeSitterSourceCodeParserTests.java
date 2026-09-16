package com.repoinsight.worker.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TreeSitterSourceCodeParserTests {

	@TempDir
	Path root;

	@Test
	void parsesJavaStructure() throws Exception {
		StructuralFileMetrics metrics = parse(new JavaSourceCodeParser(), "Demo.java", """
				import java.util.List;
				interface Contract { void execute(); }
				class Demo {
				  void run() {
				    if (true) {
				      for (int index = 0; index < 1; index++) { System.out.println(index); }
				    }
				  }
				}
				""");

		assertThat(metrics.classCount()).isEqualTo(1);
		assertThat(metrics.interfaceCount()).isEqualTo(1);
		assertThat(metrics.methodCount()).isEqualTo(2);
		assertThat(metrics.imports()).hasSize(1);
		assertThat(metrics.controlFlowCount()).isEqualTo(2);
		assertThat(metrics.maximumNestingDepth()).isEqualTo(2);
		assertThat(metrics.symbols()).extracting(StructuralSymbol::name).contains("Contract", "Demo", "run", "execute");
		assertThat(metrics.parseError()).isFalse();
	}

	@Test
	void parsesCSharpStructure() throws Exception {
		StructuralFileMetrics metrics = parse(new CSharpSourceCodeParser(), "Demo.cs", """
				using System;
				interface IRunner { void Run(); }
				class Demo : IRunner {
				  public void Run() {
				    if (true) { while (false) { Console.WriteLine("test"); } }
				  }
				}
				""");

		assertThat(metrics.classCount()).isEqualTo(1);
		assertThat(metrics.interfaceCount()).isEqualTo(1);
		assertThat(metrics.methodCount()).isEqualTo(2);
		assertThat(metrics.imports()).hasSize(1);
		assertThat(metrics.controlFlowCount()).isEqualTo(2);
		assertThat(metrics.maximumNestingDepth()).isEqualTo(2);
		assertThat(metrics.parseError()).isFalse();
	}

	@Test
	void parsesPythonFunctionsAndMethods() throws Exception {
		StructuralFileMetrics metrics = parse(new PythonSourceCodeParser(), "demo.py", """
				import os
				class Demo:
				    def run(self):
				        if True:
				            for item in [1]:
				                print(item)
				def helper():
				    return os.getcwd()
				""");

		assertThat(metrics.classCount()).isEqualTo(1);
		assertThat(metrics.methodCount()).isEqualTo(1);
		assertThat(metrics.functionCount()).isEqualTo(1);
		assertThat(metrics.imports()).hasSize(1);
		assertThat(metrics.controlFlowCount()).isEqualTo(2);
		assertThat(metrics.maximumNestingDepth()).isEqualTo(2);
		assertThat(metrics.symbols()).extracting(StructuralSymbol::name).contains("Demo", "run", "helper");
		assertThat(metrics.parseError()).isFalse();
	}

	@Test
	void parsesTypeScriptAndTsxStructure() throws Exception {
		TypeScriptSourceCodeParser parser = new TypeScriptSourceCodeParser();
		StructuralFileMetrics metrics = parse(parser, "demo.ts", """
				import { value } from './dependency'
				interface Contract { run(): void }
				class Demo implements Contract {
				  run() {
				    if (true) { for (const item of [1]) { console.log(item) } }
				  }
				}
				const helper = () => value
				""");

		assertThat(parser.supports(Path.of("component.tsx"))).isTrue();
		assertThat(metrics.classCount()).isEqualTo(1);
		assertThat(metrics.interfaceCount()).isEqualTo(1);
		assertThat(metrics.methodCount()).isEqualTo(1);
		assertThat(metrics.functionCount()).isEqualTo(1);
		assertThat(metrics.imports()).hasSize(1);
		assertThat(metrics.controlFlowCount()).isEqualTo(2);
		assertThat(metrics.maximumNestingDepth()).isEqualTo(2);
		assertThat(metrics.symbols()).extracting(StructuralSymbol::name).contains("Contract", "Demo", "run", "helper");
		assertThat(metrics.parseError()).isFalse();
	}

	private StructuralFileMetrics parse(SourceCodeParser parser, String name, String source) throws Exception {
		Path file = root.resolve(name);
		Files.writeString(file, source);
		return parser.parse(root, file);
	}
}