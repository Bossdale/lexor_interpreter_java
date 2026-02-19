package org.lexor;

import org.lexor.ast.nodes.*;
import org.lexor.ast.visitor.ASTPrinter;
import org.lexor.lexer.Token;
import org.lexor.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 2: AST & VISITOR ---");

        // 1. Manually create the Tokens that the Lexer WOULD have generated
        Token typeToken = new Token(TokenType.INT, "INT", 3);
        Token idToken = new Token(TokenType.IDENTIFIER, "_myVar", 3);
        Token valueToken = new Token(TokenType.INT_LITERAL, "10", 3);

        // 2. Manually build the AST Nodes
        // A literal node holding the number 10
        LiteralNode literal10 = new LiteralNode(valueToken);

        // A variable declaration node: INT _myVar = 10
        VarDeclNode declaration = new VarDeclNode(typeToken, idToken, literal10);

        // Put it inside lists for the ProgramNode
        List<VarDeclNode> declarations = new ArrayList<>();
        declarations.add(declaration);
        List<StatementNode> statements = new ArrayList<>();

        // Create the root of the tree
        ProgramNode program = new ProgramNode(declarations, statements);

        // 3. Test the Visitor Pattern
        ASTPrinter printer = new ASTPrinter();
        String treeString = printer.print(program);

        System.out.println(treeString);
    }
}