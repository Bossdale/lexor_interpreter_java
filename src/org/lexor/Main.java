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

        // TODO — Add --debug flag to Main.java to toggle AST and symbol table output
        //  Right now, the ASTPrinter exists but is never called anywhere.
        //  It's dead code. The Main.java also always prints "--- LEXOR OUTPUT ---" unconditionally.
        //  A --debug flag would make the ASTPrinter and symbol table dump actually usable.

        // TODO: Support a --debug flag that prints the AST and symbol table.
        //       Usage: java Main <file.lxr> [--debug]
        //       Without --debug, only program output is shown (clean mode).

        // If no file is passed via terminal, default to our test script
        String filePath = (args.length == 1) ? args[0] : "sample_scripts/test_program.lxr";

        try {
            // Read the entire .lxr file into a String
            String sourceCode = new String(Files.readAllBytes(Paths.get(filePath)));

            // PHASE 1: Lexical Analysis
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.scanTokens();

            // PHASE 2 & 3: Syntax Analysis (Parser)
            Parser parser = new Parser(tokens);
            ProgramNode astRoot = parser.parse();

            // PHASE 4: Semantic Analysis
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(astRoot);

            // PHASE 5: Execution (Interpreter)
            System.out.println("--- LEXOR OUTPUT ---");
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(astRoot);

        } catch (IOException e) {
            System.err.println("Error reading file: Could not find or open '" + filePath + "'");
        } catch (LexorException e) {
            System.err.println("\n[COMPILATION FAILED]");
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected internal error occurred: " + e.getMessage());
        }
    }
}