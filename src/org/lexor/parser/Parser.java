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
    private int lastStatementLine = -1;

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

        lastStatementLine = previous().line;

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
        int startLine = peek().line;
        if (lastStatementLine != -1 && startLine <= lastStatementLine) {
            throw new org.lexor.error.SyntaxError(startLine, "Multiple statements on a single line are not allowed.");
        }

        List<VarDeclNode> decls = new ArrayList<>();
        consume(TokenType.DECLARE, "Expected 'DECLARE' keyword.");
        Token typeToken = advance();

        do {
            Token identifier = consume(TokenType.IDENTIFIER, "Expected variable name.");
            ASTNode initializer = null;
            if (match(TokenType.EQUAL)) {
                initializer = parseAssignmentRHS();
            }
            decls.add(new VarDeclNode(typeToken, identifier, initializer));
        } while (match(TokenType.COMMA));

        lastStatementLine = previous().line; // Update the memory
        return decls;
    }

    private StatementNode parseStatement() {
        int startLine = peek().line;
        if (lastStatementLine != -1 && startLine <= lastStatementLine) {
            throw new org.lexor.error.SyntaxError(startLine, "Multiple statements on a single line are not allowed.");
        }

        StatementNode stmt;
        if (match(TokenType.PRINT)) stmt = parsePrint();
        else if (match(TokenType.SCAN)) stmt = parseScan();
        else if (match(TokenType.IF)) stmt = parseIf();
        else if (match(TokenType.REPEAT)) stmt = parseRepeat();
        else if (match(TokenType.FOR)) stmt = parseFor();
        else stmt = parseAssignmentExpression();

        lastStatementLine = previous().line; // Update the memory
        return stmt;
    }

    private ASTNode parseAssignmentRHS() {
        if (check(TokenType.IDENTIFIER) && peekNext().type == TokenType.EQUAL) {
            return parseAssignmentExpression();
        }
        return parseExpressionPDA();
    }

    private AssignmentNode parseAssignmentExpression() {
        Token name = consume(TokenType.IDENTIFIER, "Expected identifier for assignment.");
        consume(TokenType.EQUAL, "Expected '=' after identifier.");
        ASTNode value = parseAssignmentRHS();
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
        return new ScanNode(identifiers);
    }

    private StatementNode parseIf() {
        ExpressionNode condition = parseExpressionPDA();

        consume(TokenType.START, "Expected 'START' before 'IF' block.");
        consume(TokenType.IF, "Expected 'IF' after 'START'.");

        BlockNode thenBranch = parseBlock(TokenType.IF);
        List<IfNode.ElseIfPart> elseIfParts = new ArrayList<>();
        BlockNode elseBranch = null;

        while (match(TokenType.ELSE)) {
            if (match(TokenType.IF)) {

                ExpressionNode elseIfCond = parseExpressionPDA();

                consume(TokenType.START, "Expected 'START' for ELSE IF.");
                consume(TokenType.IF, "Expected 'IF' after 'START'.");
                elseIfParts.add(new IfNode.ElseIfPart(elseIfCond, parseBlock(TokenType.IF)));
            } else {
                consume(TokenType.START, "Expected 'START' for ELSE.");
                consume(TokenType.IF, "Expected 'IF' after 'START'.");
                elseBranch = parseBlock(TokenType.IF);
                break;
            }
        }
        return new IfNode(condition, thenBranch, elseIfParts, elseBranch);
    }

    private StatementNode parseRepeat() {
        consume(TokenType.WHEN, "Expected 'WHEN' after 'REPEAT'.");
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'WHEN'.");
        ExpressionNode condition = parseExpressionPDA();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after REPEAT condition.");
        consume(TokenType.START, "Expected 'START' before loop.");
        consume(TokenType.REPEAT, "Expected 'REPEAT' after 'START'.");
        return new RepeatNode(condition, parseBlock(TokenType.REPEAT));
    }

    private StatementNode parseFor() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'FOR'.");
        StatementNode init = parseAssignmentExpression();
        consume(TokenType.COMMA, "Expected ',' after init.");
        ExpressionNode cond = parseExpressionPDA();
        consume(TokenType.COMMA, "Expected ',' after condition.");
        StatementNode update = parseAssignmentExpression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after header.");

        consume(TokenType.START, "Expected 'START' before 'FOR' block.");
        consume(TokenType.FOR, "Expected 'FOR' after 'START'.");

        return new ForNode(init, cond, update, parseBlock(TokenType.FOR));
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
    // FULL DPDA FOR EXPRESSIONS
    // =========================================================================

    private ExpressionNode parseExpressionPDA() {
        Stack<ExpressionNode> nodeStack = new Stack<>();
        Stack<Token> operatorStack = new Stack<>();
        boolean expectingOperand = true;

        while (isPartOfExpression(peek().type)) {
            if (!expectingOperand && (isLiteralOrIdentifier(peek().type) || peek().type == TokenType.LEFT_PAREN)) {
                break;
            }
            Token token = advance();

            if (isLiteralOrIdentifier(token.type)) {
                if (token.type == TokenType.IDENTIFIER) {
                    nodeStack.push(new IdentifierNode(token));
                } else if (token.type == TokenType.DOLLAR) {
                    nodeStack.push(new NewlineNode());
                } else {
                    nodeStack.push(new LiteralNode(token));
                }
                expectingOperand = false;
            }
            else if (token.type == TokenType.LEFT_PAREN) {
                operatorStack.push(token);
                expectingOperand = true;
            }
            else if (token.type == TokenType.RIGHT_PAREN) {
                while (!operatorStack.isEmpty() && operatorStack.peek().type != TokenType.LEFT_PAREN) {
                    reduce(nodeStack, operatorStack);
                }
                if (operatorStack.isEmpty() || operatorStack.peek().type != TokenType.LEFT_PAREN) {
                    throw new org.lexor.error.SyntaxError(token.line, "Mismatched parentheses. Extra closing ')'.");
                }
                operatorStack.pop();
                expectingOperand = false;
            } 
            else if (isOperator(token.type)) {
                
                // 1. UNARY OPERATOR CHECK: If we are expecting a number and see a minus/plus, it is unary!
                if (expectingOperand && (token.type == TokenType.MINUS || token.type == TokenType.PLUS || token.type == TokenType.NOT)) {
                    
                    TokenType newType = (token.type == TokenType.MINUS) ? TokenType.UNARY_MINUS : 
                                        (token.type == TokenType.PLUS) ? TokenType.UNARY_PLUS : TokenType.NOT;
                    
                    Token unaryToken = new Token(newType, token.lexeme, token.line);
                    operatorStack.push(unaryToken);
                    
                } 
                // 2. BINARY OPERATOR CHECK: Otherwise, process it as normal math
                else {
                    while (!operatorStack.isEmpty() && precedence(operatorStack.peek().type) >= precedence(token.type)) {
                        reduce(nodeStack, operatorStack);
                    }
                    operatorStack.push(token);
                }
                
                expectingOperand = true; // After any operator, we expect a number next
            }
        }

        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek().type == TokenType.LEFT_PAREN) {
                throw new org.lexor.error.SyntaxError(operatorStack.peek().line, "Mismatched parentheses. Missing closing ')'.");
            }
            reduce(nodeStack, operatorStack);
        }

        if (nodeStack.isEmpty()) {
            throw new org.lexor.error.SyntaxError(peek().line, "Invalid expression.");
        }

        return nodeStack.pop();
    }

    private void reduce(Stack<ExpressionNode> nodeStack, Stack<Token> operatorStack) {
        if (operatorStack.isEmpty()) return;
        Token operator = operatorStack.pop();

        // Explicitly handle Unary Operators first
        if (operator.type == TokenType.NOT || operator.type == TokenType.UNARY_MINUS || operator.type == TokenType.UNARY_PLUS) {
            if (nodeStack.isEmpty()) throw new org.lexor.error.SyntaxError(operator.line, "Missing operand for unary operator.");
            nodeStack.push(new UnaryExprNode(operator, nodeStack.pop()));
            return;
        }

        // Explicitly handle Binary Operators
        if (nodeStack.size() < 2) {
            throw new org.lexor.error.SyntaxError(operator.line, "Missing operand for " + operator.lexeme);
        }
        
        ExpressionNode right = nodeStack.pop();
        ExpressionNode left = nodeStack.pop();

        if (operator.type == TokenType.AND || operator.type == TokenType.OR) {
            nodeStack.push(new LogicalExprNode(left, operator, right));
        } else {
            nodeStack.push(new BinaryExprNode(left, operator, right));
        }
    }

    private int precedence(TokenType type) {
        return switch (type) {
            case UNARY_MINUS, UNARY_PLUS, NOT -> 5;
            case STAR, SLASH, MODULO -> 4;
            case PLUS, MINUS, AMPERSAND -> 3;
            case GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL, NOT_EQUAL -> 2;
            case AND -> 1;
            case OR -> 0;
            default -> -1;
        };
    }

    private boolean isPartOfExpression(TokenType type) {
        return isLiteralOrIdentifier(type) || isOperator(type) ||
                type == TokenType.LEFT_PAREN || type == TokenType.RIGHT_PAREN;
    }

    private boolean isLiteralOrIdentifier(TokenType type) {
        return type == TokenType.INT_LITERAL || type == TokenType.FLOAT_LITERAL ||
                type == TokenType.CHAR_LITERAL || type == TokenType.BOOL_LITERAL ||
                type == TokenType.STRING_LITERAL || type == TokenType.ESCAPE_LITERAL ||
                type == TokenType.DOLLAR || type == TokenType.IDENTIFIER;
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS || type == TokenType.STAR || type == TokenType.SLASH ||
                type == TokenType.MODULO || type == TokenType.AMPERSAND || type == TokenType.GREATER ||
                type == TokenType.GREATER_EQUAL || type == TokenType.LESS || type == TokenType.LESS_EQUAL ||
                type == TokenType.EQUAL_EQUAL || type == TokenType.NOT_EQUAL || type == TokenType.AND ||
                type == TokenType.OR || type == TokenType.NOT;
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
        throw new org.lexor.error.SyntaxError(peek().line, message);
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

    private Token peekNext() {
        return (current + 1 >= tokens.size()) ? tokens.get(tokens.size() - 1) : tokens.get(current + 1);
    }
}