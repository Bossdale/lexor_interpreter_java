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
        while (check(TokenType.DECLARE)) {
            declarations.addAll(parseVarDeclaration());
        }

        // Parse Statements
        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(TokenType.END, "Expected 'END' keyword.");
        consume(TokenType.SCRIPT, "Expected 'SCRIPT' after 'END'.");

        return new ProgramNode(declarations, statements);
    }

    private List<VarDeclNode> parseVarDeclaration() {
        List<VarDeclNode> decls = new ArrayList<>();
        consume(TokenType.DECLARE, "Expected 'DECLARE' keyword.");
        Token typeToken = advance(); // INT, FLOAT, etc.

        do {
            Token identifier = consume(TokenType.IDENTIFIER, "Expected variable name.");
            ExpressionNode initializer = null;
            if (match(TokenType.EQUAL)) {
                initializer = parseExpressionPDA();
            }
            decls.add(new VarDeclNode(typeToken, identifier, initializer));
        } while (match(TokenType.COMMA)); // Support comma-separated variables

        consume(TokenType.DOLLAR, "Expected '$' at the end of declaration.");
        return decls;
    }

    private StatementNode parseStatement() {
        if (match(TokenType.PRINT)) return parsePrint();
        if (match(TokenType.SCAN)) return parseScan();
        if (match(TokenType.IF)) return parseIf();
        if (match(TokenType.REPEAT)) return parseRepeat();
        if (match(TokenType.FOR)) return parseFor();

        // Default to assignment if it starts with an identifier
        return parseAssignment();
    }

    private StatementNode parseAssignment() {
        StatementNode assignment = parseAssignmentExpression();
        consume(TokenType.DOLLAR, "Expected '$' at the end of assignment.");
        return assignment;
    }

    private StatementNode parseAssignmentExpression() {
        // 1. Get the variable being assigned
        Token name = consume(TokenType.IDENTIFIER, "Expected identifier for assignment.");

        // 2. Consume the equals sign
        consume(TokenType.EQUAL, "Expected '=' after identifier.");

        // 3. Parse the value (using your PDA to handle math/logic)
        ExpressionNode value = parseExpressionPDA();

        // Note: We do NOT consume TokenType.DOLLAR here!
        return new AssignmentNode(name, value);
    }

    private StatementNode parsePrint() {
        consume(TokenType.COLON, "Expected ':' after 'PRINT'.");
        List<ExpressionNode> expressions = new ArrayList<>();

        // Parse at least one expression
        expressions.add(parseExpressionPDA());

        // Handle concatenated expressions using '&'
        while (match(TokenType.AMPERSAND)) {
            expressions.add(parseExpressionPDA());
        }

        consume(TokenType.DOLLAR, "Expected '$' at the end of PRINT statement.");
        return new PrintNode(expressions);
    }

    private StatementNode parseScan() {
        consume(TokenType.COLON, "Expected ':' after 'SCAN'.");
        List<Token> identifiers = new ArrayList<>();

        // Parse at least one identifier
        identifiers.add(consume(TokenType.IDENTIFIER, "Expected identifier in SCAN."));

        // Handle multiple variables separated by commas
        while (match(TokenType.COMMA)) {
            identifiers.add(consume(TokenType.IDENTIFIER, "Expected identifier after ','."));
        }

        consume(TokenType.DOLLAR, "Expected '$' at the end of SCAN statement.");
        return new ScanNode(identifiers);
    }

    private StatementNode parseIf() {
        ExpressionNode condition = parseExpressionPDA();
        consume(TokenType.START, "Expected 'START' before 'IF' block.");
        consume(TokenType.IF, "Expected 'IF' after 'START'.");

        BlockNode thenBranch = parseBlock(TokenType.IF);
        List<IfNode.ElseIfPart> elseIfParts = new ArrayList<>();
        BlockNode elseBranch = null;

        // Handle multiple ELSE IF alternatives
        while (match(TokenType.ELSE)) {
            if (match(TokenType.IF)) {
                ExpressionNode elseIfCond = parseExpressionPDA();
                consume(TokenType.START, "Expected 'START' for ELSE IF.");
                consume(TokenType.IF, "Expected 'IF' after 'START'.");
                elseIfParts.add(new IfNode.ElseIfPart(elseIfCond, parseBlock(TokenType.IF)));
            } else {
                // Handle final ELSE
                consume(TokenType.START, "Expected 'START' for ELSE.");
                consume(TokenType.IF, "Expected 'IF' after 'START'.");
                elseBranch = parseBlock(TokenType.IF);
                break; // ELSE must be the last part
            }
        }
        return new IfNode(condition, thenBranch, elseIfParts, elseBranch);
    }

    private StatementNode parseRepeat() {
        consume(TokenType.WHEN, "Expected 'WHEN' after 'REPEAT'.");
        ExpressionNode condition = parseExpressionPDA();
        consume(TokenType.START, "Expected 'START' before loop.");
        consume(TokenType.REPEAT, "Expected 'REPEAT' after 'START'.");

        BlockNode body = parseBlock(TokenType.REPEAT);
        return new RepeatNode(condition, body);
    }

    private StatementNode parseFor() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'FOR'.");
        StatementNode init = parseAssignmentExpression(); // Helper without '$'
        consume(TokenType.COMMA, "Expected ',' after init.");
        ExpressionNode cond = parseExpressionPDA();
        consume(TokenType.COMMA, "Expected ',' after condition.");
        StatementNode update = parseAssignmentExpression(); // Helper without '$'
        consume(TokenType.RIGHT_PAREN, "Expected ')' after header.");

        consume(TokenType.START, "Expected 'START' before 'FOR' block.");
        consume(TokenType.FOR, "Expected 'FOR' after 'START'.");

        BlockNode body = parseBlock(TokenType.FOR);
        return new ForNode(init, cond, update, body);
    }

    private BlockNode parseBlock(TokenType type) {
        List<StatementNode> statements = new ArrayList<>();
        while (!check(TokenType.END) && !isAtEnd()) {
            statements.add(parseStatement());
        }
        consume(TokenType.END, "Expected 'END' for " + type);
        consume(type, "Expected " + type + " after 'END'.");
        return new BlockNode(statements);
    }

    // =========================================================================
    // FULL DPDA FOR EXPRESSIONS (Now with Parentheses support)
    // =========================================================================

    private ExpressionNode parseExpressionPDA() {
        Stack<ExpressionNode> nodeStack = new Stack<>();
        Stack<Token> operatorStack = new Stack<>();
        boolean expectingOperand = true;

        // Keep parsing while we see numbers, variables, operators, or parenthesis
        while (isPartOfExpression(peek().type)) {
            Token token = advance();

            if (isLiteralOrIdentifier(token.type)) {
                if (token.type == TokenType.IDENTIFIER) {
                    nodeStack.push(new IdentifierNode(token));
                } else {
                    nodeStack.push(new LiteralNode(token));
                }
                expectingOperand = false;
            }
            else if (token.type == TokenType.LEFT_PAREN) {
                // PDA State: Push '(' to the operator stack
                operatorStack.push(token);
                expectingOperand = true;
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
                expectingOperand = false;
            }
            else if (token.type == TokenType.DOLLAR) {
                nodeStack.push(new NewlineNode());
                expectingOperand = false;
            }
            else if (isOperator(token.type)) {
                if (expectingOperand && (token.type == TokenType.MINUS || token.type == TokenType.PLUS || token.type == TokenType.NOT)) {
                    operatorStack.push(token);
                }else {
                    while (!operatorStack.isEmpty() && precedence(operatorStack.peek().type) >= precedence(token.type)) {
                        reduce(nodeStack, operatorStack);
                    }
                    operatorStack.push(token);
                }
                expectingOperand = true;
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

        // 1. Handle Unary Operators (NOT, unary -, unary +)
        if (isUnary(operator.type)) {
            if (nodeStack.isEmpty()) throw new RuntimeException("Syntax Error: Missing operand for unary operator " + operator.lexeme);
            ExpressionNode right = nodeStack.pop();
            nodeStack.push(new UnaryExprNode(operator, right));
        }
        // 2. Handle Binary/Logical Operators
        else {
            if (nodeStack.size() < 2) throw new RuntimeException("Syntax Error: Missing operand for operator " + operator.lexeme);
            ExpressionNode right = nodeStack.pop();
            ExpressionNode left = nodeStack.pop();

            if (operator.type == TokenType.AND || operator.type == TokenType.OR) {
                nodeStack.push(new LogicalExprNode(left, operator, right));
            } else {
                nodeStack.push(new BinaryExprNode(left, operator, right));
            }
        }
    }

    private boolean isUnary(TokenType type) {
        return type == TokenType.NOT ||
                type == TokenType.MINUS ||
                type == TokenType.PLUS;
    }

    private int precedence(TokenType type) {
        return switch (type) {
            // Multiplicative operators (Highest arithmetic)
            case STAR, SLASH, MODULO -> 4;

            // Additive and Concatenation operators
            case PLUS, MINUS, AMPERSAND -> 3;

            // Relational / Comparison operators
            case GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL, NOT_EQUAL -> 2;

            // Logical AND
            case AND -> 1;

            // Logical OR (Lowest priority)
            case OR -> 0;

            // Parentheses and others have no precedence relative to operators
            default -> -1;
        };
    }

    private boolean isPartOfExpression(TokenType type) {
        return isLiteralOrIdentifier(type) || isOperator(type) ||
                type == TokenType.LEFT_PAREN || type == TokenType.RIGHT_PAREN ||
                type == TokenType.DOLLAR; // Allow NewlineNode in expressions
    }

    private boolean isLiteralOrIdentifier(TokenType type) {
        return type == TokenType.INT_LITERAL || type == TokenType.FLOAT_LITERAL ||
                type == TokenType.CHAR_LITERAL || type == TokenType.BOOL_LITERAL ||
                type == TokenType.IDENTIFIER;
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS ||
                type == TokenType.STAR || type == TokenType.SLASH ||
                type == TokenType.MODULO || type == TokenType.AMPERSAND ||
                type == TokenType.GREATER || type == TokenType.GREATER_EQUAL ||
                type == TokenType.LESS || type == TokenType.LESS_EQUAL ||
                type == TokenType.EQUAL_EQUAL || type == TokenType.NOT_EQUAL ||
                type == TokenType.AND || type == TokenType.OR || type == TokenType.NOT;
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