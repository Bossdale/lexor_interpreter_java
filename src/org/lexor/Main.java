package org.lexor;

import org.lexor.ast.nodes.ProgramNode;
import org.lexor.lexer.Lexer;
import org.lexor.lexer.Token;
import org.lexor.parser.Parser;
import org.lexor.semantic.SemanticAnalyzer;
import org.lexor.runtime.Interpreter;
import org.lexor.error.LexorException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        boolean debugMode = false;
        String filePath = "sample_scripts/test_program.lxr";

        for (String arg : args) {
            if (arg.equals("--debug")) {
                debugMode = true;
            } else {
                filePath = arg;
            }
        }

        try {
            String sourceCode = new String(Files.readAllBytes(Paths.get(filePath)));

            // PHASE 1: Lexical Analysis
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.scanTokens();

            if (debugMode) {
                System.out.println("=== PHASE 1: TOKENS ===");
                for (Token t : tokens) {
                    System.out.printf("  %-20s | %-15s | Line %d%n",
                            t.type, t.lexeme, t.line);
                }
            }

            // PHASE 2: Parsing
            Parser parser = new Parser(tokens);
            ProgramNode astRoot = parser.parse();

            if (debugMode) {
                System.out.println("\n=== PHASE 2: AST ===");
                // TODO: ASTPrinter is already implemented — wire it in here.
                org.lexor.ast.visitor.ASTPrinter printer = new org.lexor.ast.visitor.ASTPrinter();
                System.out.println(printer.print(astRoot));
            }

            // PHASE 3: Semantic Analysis
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(astRoot);

            if (debugMode) {
                System.out.println("\n=== PHASE 3: SEMANTIC ANALYSIS PASSED ===");
                System.out.println(analyzer.getSymbolTable().dump());
            }

            // PHASE 4: Execution
            if (!debugMode) System.out.println("\n--- LEXOR OUTPUT ---");
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(astRoot);

            // In Main.java, update the catch blocks at the bottom:
        } catch (IOException e) {
            System.err.println("Error reading file: Could not find or open '" + filePath + "'");
        } catch (org.lexor.error.RuntimeError e) {
            System.err.println("\n[EXECUTION FAILED]");
            System.err.println(e.getMessage());
        } catch (LexorException e) {
            System.err.println("\n[COMPILATION FAILED]");
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected internal error occurred: " + e.getMessage());
        }
    }
}