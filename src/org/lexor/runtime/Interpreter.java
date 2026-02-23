package org.lexor.runtime;

import org.lexor.ast.nodes.*;
import org.lexor.ast.visitor.ASTVisitor;
import org.lexor.lexer.TokenType;
import org.lexor.runtime.values.*;

import java.util.Scanner;

// Interprets the validated AST by executing its nodes sequentially
public class Interpreter implements ASTVisitor<RuntimeValue> {

    private final Environment environment;
    private final Scanner inputScanner;

    public Interpreter() {
        this.environment = new Environment();
        this.inputScanner = new Scanner(System.in);
    }

    // Main entry point for Phase 4
    public void interpret(ProgramNode program) {
        try {
            program.accept(this);
        } catch (Exception e) {
            System.err.println("Runtime Error: " + e.getMessage());
        }
    }

    // =========================================================================
    // STATEMENT VISITORS (Controlling Execution Flow)
    // =========================================================================

    @Override
    public RuntimeValue visitProgramNode(ProgramNode node) {
        // 1. Initialize variables in memory [cite: 24]
        for (VarDeclNode decl : node.declarations) {
            decl.accept(this);
        }

        // 2. Execute the statements in order [cite: 28]
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public RuntimeValue visitVarDeclNode(VarDeclNode node) {
        RuntimeValue value = null;

        // If it's initialized (e.g., DECLARE INT x = 5), evaluate the initializer
        if (node.initializer != null) {
            value = node.initializer.accept(this);
        } else {
            // Otherwise, assign a default value based on type [cite: 33-36]
            value = switch (node.dataType.type) {
                case INT -> new IntValue(0);
                case FLOAT -> new FloatValue(0.0f);
                case BOOL -> new BoolValue(false);
                case CHAR -> new CharValue('\0');
                default -> throw new RuntimeException("Unknown data type declaration.");
            };
        }

        // Store it in the runtime memory
        environment.define(node.identifier.lexeme, value);
        return null;
    }

    @Override
    public RuntimeValue visitAssignmentNode(AssignmentNode node) {
        RuntimeValue value = node.value.accept(this);
        environment.assign(node.identifier.lexeme, value);
        return null;
    }

    @Override
    public RuntimeValue visitPrintNode(PrintNode node) {
        // LEXOR prints separated by ampersands, which means we just evaluate and concatenate them seamlessly[cite: 15, 18, 31].
        for (ExpressionNode expr : node.getExpressions()) {
            RuntimeValue val = expr.accept(this);
            System.out.print(val.asString());
        }
        return null;
    }

    @Override
    public RuntimeValue visitScanNode(ScanNode node) {
        // Reads input for multiple variables dynamically
        for (org.lexor.lexer.Token id : node.identifiers) {
            String name = id.lexeme;
            RuntimeValue currentVal = environment.get(name);

            // Using Java's Scanner to read the exact primitive type
            if (currentVal instanceof IntValue) {
                environment.assign(name, new IntValue(inputScanner.nextInt()));
            } else if (currentVal instanceof FloatValue) {
                environment.assign(name, new FloatValue(inputScanner.nextFloat()));
            } else if (currentVal instanceof BoolValue) {
                String input = inputScanner.next();
                // Match standard input or LEXOR's "TRUE" literal format [cite: 11]
                boolean isTrue = input.equalsIgnoreCase("\"TRUE\"") || input.equalsIgnoreCase("TRUE");
                environment.assign(name, new BoolValue(isTrue));
            } else if (currentVal instanceof CharValue) {
                environment.assign(name, new CharValue(inputScanner.next().charAt(0)));
            }
        }
        return null;
    }

    @Override
    public RuntimeValue visitIfNode(IfNode node) {
        // Check the primary IF condition [cite: 87, 93, 104]
        if ((boolean) node.condition.accept(this).getValue()) {
            node.thenBranch.accept(this);
        } else {
            boolean matched = false;
            // Iterate through ELSE IF parts if any [cite: 109]
            for (IfNode.ElseIfPart part : node.elseIfParts) {
                if ((boolean) part.condition.accept(this).getValue()) {
                    part.body.accept(this);
                    matched = true;
                    break;
                }
            }
            // Execute ELSE block if no previous condition was met [cite: 98, 114]
            if (!matched && node.elseBranch != null) {
                node.elseBranch.accept(this);
            }
        }
        return null;
    }

    @Override
    public RuntimeValue visitRepeatNode(RepeatNode node) {
        // Standard Do-While loop: Repeats WHEN condition is true [cite: 125]
        do {
            node.body.accept(this);
        } while ((boolean) node.condition.accept(this).getValue());
        return null;
    }

    @Override
    public RuntimeValue visitForNode(ForNode node) {
        // Executes standard FOR loop: init -> condition -> update [cite: 120]
        for (node.initialization.accept(this);
             (boolean) node.condition.accept(this).getValue();
             node.update.accept(this)) {
            node.body.accept(this);
        }
        return null;
    }

    @Override
    public RuntimeValue visitBlockNode(BlockNode node) {
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    // =========================================================================
    // EXPRESSION VISITORS (Calculating Values)
    // =========================================================================

    @Override
    public RuntimeValue visitLiteralNode(LiteralNode node) {
        String lexeme = node.valueToken.lexeme;
        return switch (node.valueToken.type) {
            case INT_LITERAL -> new IntValue(Integer.parseInt(lexeme));
            case FLOAT_LITERAL -> new FloatValue(Float.parseFloat(lexeme));
            case BOOL_LITERAL -> new BoolValue(lexeme.contains("TRUE")); // Handles "TRUE" or TRUE
            case CHAR_LITERAL -> {
                // Strip the single quotes from the character literal (e.g., 'c')
                if (lexeme.length() >= 3) yield new CharValue(lexeme.charAt(1));
                yield new CharValue(lexeme.charAt(0));
            }
            default -> throw new RuntimeException("Unrecognized literal format.");
        };
    }

    @Override
    public RuntimeValue visitIdentifierNode(IdentifierNode node) {
        return environment.get(node.name.lexeme);
    }

    @Override
    public RuntimeValue visitBinaryExprNode(BinaryExprNode node) {
        RuntimeValue left = node.left.accept(this);
        RuntimeValue right = node.right.accept(this);

        // If the operator is an ampersand, return an anonymous string representation [cite: 31]
        if (node.operator.type == TokenType.AMPERSAND) {
            return new RuntimeValue() {
                @Override public Object getValue() { return left.asString() + right.asString(); }
                @Override public String asString() { return left.asString() + right.asString(); }
            };
        }

        // Float arithmetic promotion
        boolean isFloat = (left instanceof FloatValue || right instanceof FloatValue);
        float leftF = Float.parseFloat(left.asString());
        float rightF = Float.parseFloat(right.asString());

        return switch (node.operator.type) {
            // Arithmetic
            case PLUS -> isFloat ? new FloatValue(leftF + rightF) : new IntValue((int)(leftF + rightF));
            case MINUS -> isFloat ? new FloatValue(leftF - rightF) : new IntValue((int)(leftF - rightF));
            case STAR -> isFloat ? new FloatValue(leftF * rightF) : new IntValue((int)(leftF * rightF));
            case SLASH -> isFloat ? new FloatValue(leftF / rightF) : new IntValue((int)(leftF / rightF));
            case MODULO -> isFloat ? new FloatValue(leftF % rightF) : new IntValue((int)(leftF % rightF));

            // Relational [cite: 42-44, 48-50]
            case GREATER -> new BoolValue(leftF > rightF);
            case LESS -> new BoolValue(leftF < rightF);
            case GREATER_EQUAL -> new BoolValue(leftF >= rightF);
            case LESS_EQUAL -> new BoolValue(leftF <= rightF);
            case EQUAL_EQUAL -> new BoolValue(left.getValue().equals(right.getValue()));
            case NOT_EQUAL -> new BoolValue(!left.getValue().equals(right.getValue()));

            default -> throw new RuntimeException("Unknown binary operator.");
        };
    }

    @Override
    public RuntimeValue visitLogicalExprNode(LogicalExprNode node) {
        boolean leftVal = (boolean) node.left.accept(this).getValue();
        boolean rightVal = (boolean) node.right.accept(this).getValue();

        // AND requires both to be true; OR requires at least one [cite: 51-52]
        return switch (node.operator.type) {
            case AND -> new BoolValue(leftVal && rightVal);
            case OR -> new BoolValue(leftVal || rightVal);
            default -> throw new RuntimeException("Unknown logical operator.");
        };
    }

    @Override
    public RuntimeValue visitUnaryExprNode(UnaryExprNode node) {
        RuntimeValue right = node.right.accept(this);

        // NOT operator reverses BOOL values [cite: 53-54]
        if (node.operator.type == TokenType.NOT) {
            return new BoolValue(!(boolean) right.getValue());
        }

        // Positive/Negative unary operators [cite: 55-57]
        float value = Float.parseFloat(right.asString());
        boolean isFloat = right instanceof FloatValue;

        if (node.operator.type == TokenType.MINUS) {
            return isFloat ? new FloatValue(-value) : new IntValue((int)-value);
        }

        // Handles TokenType.PLUS natively
        return right;
    }

    @Override
    public RuntimeValue visitGroupingNode(GroupingNode node) {
        return node.expression.accept(this);
    }

    @Override
    public RuntimeValue visitNewlineNode(NewlineNode node) {
        // The $ token produces a carriage return/newline character [cite: 30]
        return new CharValue('\n');
    }
}