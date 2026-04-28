package org.lexor.semantic;

import org.lexor.ast.nodes.*;
import org.lexor.ast.visitor.ASTVisitor;
import org.lexor.lexer.TokenType;
import org.lexor.semantic.symbol.Symbol;
import org.lexor.semantic.symbol.SymbolTable;
import org.lexor.semantic.symbol.Type;

public class SemanticAnalyzer implements ASTVisitor<Type> {
    private SymbolTable currentScope;

    public SemanticAnalyzer() {
        this.currentScope = new SymbolTable();
    }

    // Main entry point for Phase 3
    public void analyze(ProgramNode program) {
        program.accept(this);
    }

    // Helper to map Lexer Tokens to Semantic Types
    private Type determineType(TokenType tokenType) {
        return switch (tokenType) {
            case INT -> Type.INT;
            case FLOAT -> Type.FLOAT;
            case CHAR -> Type.CHAR;
            case BOOL -> Type.BOOL;
            default -> Type.UNKNOWN;
        };
    }

    // =========================================================================
    // STATEMENT VISITORS
    // =========================================================================


    // Performs a two-pass semantic check: registers all declarations first, then validates all executable statements.
    @Override
    public Type visitProgramNode(ProgramNode node) {
        for (VarDeclNode decl : node.declarations) {
            decl.accept(this);
        }

        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    // Registers the variable in the symbol table and strictly type-checks its optional initial value.
    @Override
    public Type visitVarDeclNode(VarDeclNode decl) {
        String varName = decl.identifier.lexeme;
        Type type = determineType(decl.dataType.type);

        boolean hasInit = decl.initializer != null;
        currentScope.define(varName, type, decl.identifier.line, hasInit);

        if (hasInit) {
            Type initializerType = decl.initializer.accept(this);
            if (type != initializerType && !(type == Type.FLOAT && initializerType == Type.INT)) {
                throw new org.lexor.error.SemanticError(decl.identifier.line,
                        "Type mismatch. Cannot assign " + initializerType +
                                " to " + type + " variable '" + varName + "'.");
            }
        }
        return null;
    }

    // Evaluates and returns the data type of the expression enclosed in parentheses.
    @Override
    public Type visitGroupingNode(GroupingNode node) {
        return node.expression.accept(this);
    }

    // Verifies the variable exists and ensures the assigned value strictly matches its declared type.
    @Override
    public Type visitAssignmentNode(AssignmentNode node) {
        Symbol sym = currentScope.resolve(node.identifier.lexeme, node.identifier.line);
        Type varType = sym.getType();
        Type valueType = node.value.accept(this);

        if (varType != valueType && !(varType == Type.FLOAT && valueType == Type.INT)) {
            throw new org.lexor.error.SemanticError(node.identifier.line,
                    "Type mismatch in assignment. Cannot assign " + valueType + " to " + varType + ".");
        }

        sym.markInitialized();
        return varType;
    }

    // Evaluates each expression to be printed to ensure variables exist and operations are valid.
    @Override
    public Type visitPrintNode(PrintNode node) {
        for (ExpressionNode expr : node.expressions) {
            expr.accept(this);
        }
        return null;
    }

    // Verifies that all variables targeted for input have been previously declared.
    @Override
    public Type visitScanNode(ScanNode node) {
        for (org.lexor.lexer.Token id : node.identifiers) {
            Symbol sym = currentScope.resolve(id.lexeme, id.line);
            sym.markInitialized();
        }
        return null;
    }

    // Validates that all conditions evaluate to a BOOL and semantically checks every execution branch.
    @Override
    public Type visitIfNode(IfNode node) {
        Type condType = node.condition.accept(this);
        if (condType != Type.BOOL) {
            throw new org.lexor.error.SemanticError(-1, "IF condition must evaluate to a BOOL.");
        }
        node.thenBranch.accept(this);

        for (IfNode.ElseIfPart elseIf : node.elseIfParts) {
            if (elseIf.condition.accept(this) != Type.BOOL) {
                throw new org.lexor.error.SemanticError(-1, "ELSE IF condition must evaluate to a BOOL.");
            }
            elseIf.body.accept(this);
        }

        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }

    // Ensures the loop condition evaluates to a BOOL and validates the statements within its body.
    @Override
    public Type visitRepeatNode(RepeatNode node) {
        Type condType = node.condition.accept(this);
        if (condType != Type.BOOL) {
            throw new org.lexor.error.SemanticError(-1, "REPEAT WHEN condition must evaluate to a BOOL.");
        }
        node.body.accept(this);
        return null;
    }

    // Validates the loop's initialization and update steps, enforces a BOOL condition, and checks the body.
    @Override
    public Type visitForNode(ForNode node) {
        node.initialization.accept(this);
        Type condType = node.condition.accept(this);
        if (condType != Type.BOOL) {
            throw new org.lexor.error.SemanticError(-1, "FOR loop condition must evaluate to a BOOL.");
        }
        node.update.accept(this);
        node.body.accept(this);
        return null;
    }

    // Sequentially validates all statements contained within a code block.
    @Override
    public Type visitBlockNode(BlockNode node) {
        // Push a new local scope
        SymbolTable previousScope = this.currentScope;
        this.currentScope = new SymbolTable(previousScope);

        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }

        // Pop the local scope when the block ends
        this.currentScope = previousScope;
        return null;
    }


    // =========================================================================
    // EXPRESSION VISITORS (Evaluating Data Types)
    // =========================================================================


    // Maps a literal token to its corresponding semantic data type for type checking.
    @Override
    public Type visitLiteralNode(LiteralNode node) {
        return switch (node.valueToken.type) {
            case INT_LITERAL -> Type.INT;
            case FLOAT_LITERAL -> Type.FLOAT;
            case CHAR_LITERAL -> Type.CHAR;
            case BOOL_LITERAL -> Type.BOOL;
            case STRING_LITERAL -> Type.STRING;
            default -> Type.UNKNOWN;
        };
    }

    // Retrieves the variable's declared type from the symbol table, verifying its existence.

    @Override
    public Type visitIdentifierNode(IdentifierNode node) {
        Symbol sym = currentScope.resolve(node.name.lexeme, node.name.line);

        // TODO: Warn (or error) when a variable is read before being assigned any value.
        //       This catches bugs like: DECLARE INT x  then  PRINT: x  with no assignment.
        if (!sym.isInitialized()) {
            throw new org.lexor.error.SemanticError(node.name.line,
                    "Variable '" + node.name.lexeme + "' is used before being initialized.");
        }

        return sym.getType();
    }

    // Evaluates both operands to determine the resulting data type based on the specific binary operator.
    @Override
    public Type visitBinaryExprNode(BinaryExprNode node) {
        Type leftType = node.left.accept(this);
        Type rightType = node.right.accept(this);

        if (node.operator.type == TokenType.AMPERSAND) {
            return Type.STRING;
        }
        if (isRelationalOperator(node.operator.type)) {
            return Type.BOOL;
        }

        boolean leftNumeric  = (leftType == Type.INT || leftType == Type.FLOAT);
        boolean rightNumeric = (rightType == Type.INT || rightType == Type.FLOAT);

        if (!leftNumeric || !rightNumeric) {
            throw new org.lexor.error.SemanticError(
                    node.operator.line,
                    "Operator '" + node.operator.lexeme + "' requires numeric (INT or FLOAT) operands, " +
                            "but got " + leftType + " and " + rightType + "."
            );
        }

        if (leftType == Type.FLOAT || rightType == Type.FLOAT) {
            return Type.FLOAT;
        }
        return Type.INT;
    }

    // Enforces that both operands are booleans and evaluates the logical expression to a BOOL type.
    @Override
    public Type visitLogicalExprNode(LogicalExprNode node) {
        Type leftType = node.left.accept(this);
        Type rightType = node.right.accept(this);

        if (leftType != Type.BOOL || rightType != Type.BOOL) {
            throw new org.lexor.error.SemanticError(node.operator.line, "Logical operators AND/OR require BOOL operands.");
        }
        return Type.BOOL;
    }

    // Validates the operand type for unary operators, enforcing BOOL for NOT and numeric types for signs.
    @Override
    public Type visitUnaryExprNode(UnaryExprNode node) {
        Type rightType = node.right.accept(this);

        if (node.operator.type == TokenType.NOT) {
            if (rightType != Type.BOOL) throw new org.lexor.error.SemanticError(node.operator.line, "NOT requires a BOOL.");
            return Type.BOOL;
        }

        if (rightType != Type.INT && rightType != Type.FLOAT) {
            throw new org.lexor.error.SemanticError(node.operator.line, "Unary +/- requires an INT or FLOAT.");
        }
        return rightType;
    }

    // Evaluates the newline symbol as a CHAR type to facilitate concatenation and printing.
    @Override
    public Type visitNewlineNode(NewlineNode node) {
        return Type.CHAR;
    }

    // Identifies whether a given token type is a relational operator used for boolean comparisons.
    private boolean isRelationalOperator(TokenType type) {
        return type == TokenType.GREATER || type == TokenType.GREATER_EQUAL ||
                type == TokenType.LESS || type == TokenType.LESS_EQUAL ||
                type == TokenType.EQUAL_EQUAL || type == TokenType.NOT_EQUAL;
    }

    public SymbolTable getSymbolTable() {
        return currentScope;
    }
}