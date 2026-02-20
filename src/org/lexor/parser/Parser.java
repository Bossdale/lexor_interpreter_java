package org.lexor.parser;

import org.lexor.ast.nodes.*;
import org.lexor.lexer.Token;
import org.lexor.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ProgramNode parse() {
        return parseProgram();
    }

    private ProgramNode parseProgram() {
        consume(TokenType.SCRIPT, "Expected 'SCRIPT' at the beginning.");
        consume(TokenType.AREA, "Expected 'AREA' after 'SCRIPT'.");
        consume(TokenType.START, "Expected 'START' keyword.");
        consume(TokenType.SCRIPT, "Expected 'SCRIPT' after 'START'.");

        List<VarDeclNode> declarations = new ArrayList<>();
        List<StatementNode> statements = new ArrayList<>();

        // Parse Declarations
        while (isDataType(peek().type)) {
            declarations.add(parseVarDeclaration());
        }

        // Parse Statements
        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(TokenType.END, "Expected 'END' keyword.");
        consume(TokenType.SCRIPT, "Expected 'SCRIPT' after 'END'.");

        return new ProgramNode(declarations, statements);
    }

    private VarDeclNode parseVarDeclaration() {
        Token typeToken = advance();
        Token identifier = consume(TokenType.IDENTIFIER, "Expected variable name.");

        ExpressionNode initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = parseExpressionPDA();
        }

        // Spec: "Every line contains a single statement"
        // We expect a DOLLAR token ($) to mark the end of the line/statement
        consume(TokenType.DOLLAR, "Expected '$' at the end of the declaration.");

        return new VarDeclNode(typeToken, identifier, initializer);
    }

    private StatementNode parseStatement() {
        Token identifier = consume(TokenType.IDENTIFIER, "Expected variable name.");
        consume(TokenType.EQUAL, "Expected '=' after variable name.");

        ExpressionNode value = parseExpressionPDA();

        // Spec: End the statement with the $ next-line marker
        consume(TokenType.DOLLAR, "Expected '$' at the end of the statement.");

        return new AssignmentNode(identifier, value);
    }

    // =========================================================================
    // FULL DPDA FOR EXPRESSIONS (Now with Parentheses support)
    // =========================================================================

    private ExpressionNode parseExpressionPDA() {
        Stack<ExpressionNode> nodeStack = new Stack<>();
        Stack<Token> operatorStack = new Stack<>();

        // Keep parsing while we see numbers, variables, operators, or parenthesis
        while (isPartOfExpression(peek().type)) {
            Token token = advance();

            if (isLiteralOrIdentifier(token.type)) {
                if (token.type == TokenType.IDENTIFIER) {
                    nodeStack.push(new IdentifierNode(token));
                } else {
                    nodeStack.push(new LiteralNode(token));
                }
            }
            else if (token.type == TokenType.LEFT_PAREN) {
                // PDA State: Push '(' to the operator stack
                operatorStack.push(token);
            }
            else if (token.type == TokenType.RIGHT_PAREN) {
                // PDA State: Pop and reduce until we find the matching '('
                while (!operatorStack.isEmpty() && operatorStack.peek().type != TokenType.LEFT_PAREN) {
                    reduce(nodeStack, operatorStack);
                }
                if (operatorStack.isEmpty() || operatorStack.peek().type != TokenType.LEFT_PAREN) {
                    throw new RuntimeException("Syntax Error at line " + token.line + ": Mismatched parentheses.");
                }
                operatorStack.pop(); // Discard the '(' token
            }
            else if (isOperator(token.type)) {
                while (!operatorStack.isEmpty() && precedence(operatorStack.peek().type) >= precedence(token.type)) {
                    reduce(nodeStack, operatorStack);
                }
                operatorStack.push(token);
            }
        }

        // Final State: Reduce any remaining operators
        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek().type == TokenType.LEFT_PAREN) {
                throw new RuntimeException("Syntax Error: Mismatched parentheses.");
            }
            reduce(nodeStack, operatorStack);
        }

        if (nodeStack.isEmpty()) {
            throw new RuntimeException("Syntax Error: Invalid expression at line " + peek().line);
        }

        return nodeStack.pop();
    }

    private void reduce(Stack<ExpressionNode> nodeStack, Stack<Token> operatorStack) {
        Token operator = operatorStack.pop();
        ExpressionNode right = nodeStack.pop();
        ExpressionNode left = nodeStack.pop();
        nodeStack.push(new BinaryExprNode(left, operator, right));
    }

    private int precedence(TokenType type) {
        return switch (type) {
            case STAR, SLASH, MODULO -> 2;
            case PLUS, MINUS, AMPERSAND -> 1;
            default -> 0; // LEFT_PAREN gets 0 so it stays on stack until RIGHT_PAREN forces a reduce
        };
    }

    private boolean isPartOfExpression(TokenType type) {
        return isLiteralOrIdentifier(type) || isOperator(type) ||
                type == TokenType.LEFT_PAREN || type == TokenType.RIGHT_PAREN;
    }

    private boolean isLiteralOrIdentifier(TokenType type) {
        return type == TokenType.INT_LITERAL || type == TokenType.FLOAT_LITERAL ||
                type == TokenType.CHAR_LITERAL || type == TokenType.BOOL_LITERAL ||
                type == TokenType.IDENTIFIER;
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS ||
                type == TokenType.STAR || type == TokenType.SLASH ||
                type == TokenType.MODULO || type == TokenType.AMPERSAND;
    }

    private boolean isDataType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || type == TokenType.CHAR || type == TokenType.BOOL;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException("Syntax Error at line " + peek().line + ": " + message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}