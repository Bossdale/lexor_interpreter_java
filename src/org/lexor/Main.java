package org.lexor;

import org.lexor.ast.nodes.ProgramNode;
import org.lexor.ast.visitor.ASTPrinter;
import org.lexor.lexer.Lexer;
import org.lexor.lexer.Token;
import org.lexor.parser.Parser;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 3: LEXER + PARSER PIPELINE ---");

        // 1. Write a valid LEXOR script
        // Note the '$' at the end of statements as required by the spec!
        String sourceCode =
                "SCRIPT AREA\n" +
                        "START SCRIPT\n" +
                        "INT _myVar = 10 $\n" +
                        "FLOAT _pi = 3.14 $\n" +
                        "_myVar = 10 + 5 * (2 + 3) $\n" +
                        "END SCRIPT";

        System.out.println("Source Code:");
        System.out.println(sourceCode);
        System.out.println("\n------------------------------------------------\n");

        // 2. PHASE 1: Lexical Analysis
        Lexer lexer = new Lexer(sourceCode);
        List<Token> tokens = lexer.scanTokens();
        System.out.println("Lexer: Successfully scanned " + tokens.size() + " tokens.");

        // 3. PHASE 3: Syntax Analysis (Parsing using the Pushdown Automaton)
        Parser parser = new Parser(tokens);
        ProgramNode astRoot = parser.parse();
        System.out.println("Parser: Successfully built the Abstract Syntax Tree.");

        // 4. Print the generated AST to verify the structure and math precedence
        System.out.println("\n--- GENERATED AST ---");
        ASTPrinter printer = new ASTPrinter();
        String treeString = printer.print(astRoot);
        System.out.println(treeString);
    }
}