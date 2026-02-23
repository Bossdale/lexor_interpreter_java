package org.lexor;

import org.lexor.ast.nodes.ProgramNode;
import org.lexor.lexer.Lexer;
import org.lexor.lexer.Token;
import org.lexor.parser.Parser;

import java.util.List;
import org.lexor.semantic.SemanticAnalyzer;
import org.lexor.runtime.Interpreter;

public class Main {
    public static void main(String[] args) {

        String sourceCode = "SCRIPT AREA\n" +
                "START SCRIPT\n" +
                "DECLARE INT _myVar = 10 $\n" +
                "DECLARE FLOAT _pi = 3.14 $\n" +
                "_myVar = 10 + 5 * (2 + 3) $\n" +
                "PRINT: _myVar & \"hi\" $\n" + // Added PRINT to see results
                "END SCRIPT";

        // 2. PHASE 1: Lexical Analysis
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.scanTokens();

        // 3. PHASE 2: Syntax Analysis (Parser)
        Parser parser = new Parser(tokens);
        ProgramNode astRoot = parser.parse();

        // --- NEW STEPS TO ACTIVATE YOUR CLASSES ---

        // 4. PHASE 3: Semantic Analysis
        System.out.println("\n--- PHASE 3: SEMANTIC ANALYSIS ---");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(astRoot);
        System.out.println("Semantic Check: Passed (Types and declarations are valid).");

        // 5. PHASE 4: Execution (Interpreter)
        System.out.println("\n--- PHASE 4: INTERPRETER EXECUTION ---");
        System.out.println("Output:");
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(astRoot);
    }
}