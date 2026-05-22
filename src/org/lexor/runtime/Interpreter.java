package org.lexor.runtime;

import org.lexor.ast.nodes.*;
import org.lexor.ast.visitor.ASTVisitor;
import org.lexor.error.RuntimeError;
import org.lexor.lexer.TokenType;
import org.lexor.runtime.values.*;

import java.util.Scanner;

// Interprets the validated AST by executing its nodes sequentially
public class Interpreter implements ASTVisitor<RuntimeValue> {

    private Environment environment;
    private final Scanner inputScanner;

    public Interpreter() {
        this.environment = new Environment();
        this.inputScanner = new Scanner(System.in);
    }

    // Main entry point for Phase 4
    public void interpret(ProgramNode program) {
        program.accept(this);
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
            
            // Implicit casting from INT to FLOAT on declaration ---
            if (node.dataType.type == TokenType.FLOAT && value instanceof IntValue) {
                int rawInt = (Integer) value.getValue();
                value = new FloatValue((float) rawInt);
            }
            // --------------------------------------------------------------
            
        } else {
            // Otherwise, assign a default value based on type
            value = switch (node.dataType.type) {
                case INT -> new IntValue(0);
                case FLOAT -> new FloatValue(0.0f);
                case BOOL -> new BoolValue(false);
                case CHAR -> new CharValue('\0');
                default -> throw new RuntimeError("Unknown data type declaration.");
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
        return value;
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
        // LEXOR spec: multiple values are separated by comma
        String line = inputScanner.nextLine().trim();
        String[] parts = line.split("\\s*,\\s*");

        for (int i = 0; i < node.identifiers.size(); i++) {
            String name = node.identifiers.get(i).lexeme;
            RuntimeValue currentVal = environment.get(name);
            String rawInput = (i < parts.length) ? parts[i].trim() : "";

            try {
                if (currentVal instanceof IntValue) {
                    environment.assign(name, new IntValue(Integer.parseInt(rawInput)));
                } else if (currentVal instanceof FloatValue) {
                    environment.assign(name, new FloatValue(Float.parseFloat(rawInput)));
                } else if (currentVal instanceof BoolValue) {
                    if (!rawInput.equals("TRUE") && !rawInput.equals("FALSE")) {
                        throw new RuntimeError(
                            "Invalid BOOL input '" + rawInput + "' for variable '" + name +
                            "'. Expected exactly TRUE or FALSE in uppercase."
                        );
                    }
                    environment.assign(name, new BoolValue(rawInput.equals("TRUE")));
                } else if (currentVal instanceof CharValue) {
                    if (rawInput.length() != 1) {
                        throw new RuntimeError("Invalid CHAR input. Expected a single character but got: '" + rawInput + "'.");
                    }
                    environment.assign(name, new CharValue(rawInput.charAt(0)));
                }
            } catch (NumberFormatException e) {
                throw new RuntimeError("Invalid input '" + rawInput + "' for variable '" + name + "'.");
            }
        }

        if (parts.length > node.identifiers.size()) {
            throw new RuntimeError("Too many inputs provided. Expected " +
                    node.identifiers.size() + " value(s), but got " + parts.length + ".");
        }

        return null;
    }

    @Override
    public RuntimeValue visitIfNode(IfNode node) {
        // Check the primary IF condition [cite: 87, 93, 104]
        if (isTruthy(node.condition.accept(this))) {
            node.thenBranch.accept(this);
        } else {
            boolean matched = false;
            // Iterate through ELSE IF parts if any [cite: 109]
            for (IfNode.ElseIfPart part : node.elseIfParts) {
                if (isTruthy(part.condition.accept(this))) {
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

    private boolean isTruthy(RuntimeValue val) {
        Object v = val.getValue();
        if (v instanceof Boolean b) return b;
        throw new RuntimeError("Expected a BOOL condition but got: " + val.asString());
    }

    @Override
    public RuntimeValue visitRepeatNode(RepeatNode node) {
        // REPEAT WHEN is a do-while: execute body first, stop WHEN condition becomes true
        do {
            node.body.accept(this);
        } while (!isTruthy(node.condition.accept(this)));
        return null;
    }

    @Override
    public RuntimeValue visitForNode(ForNode node) {
        for (node.initialization.accept(this);
             isTruthy(node.condition.accept(this));
             node.update.accept(this)) {
            node.body.accept(this);
        }
        return null;
    }

    @Override
    public RuntimeValue visitBlockNode(BlockNode node) {
        Environment previous = this.environment;
        this.environment = new Environment(previous);

        try {
            for (StatementNode stmt : node.statements) {
                stmt.accept(this);
            }
        } finally {
            this.environment = previous;
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
            case BOOL_LITERAL -> {
                if (lexeme.equals("\"TRUE\"") || lexeme.equals("TRUE")) {
                    yield new BoolValue(true);
                } else if (lexeme.equals("\"FALSE\"") || lexeme.equals("FALSE")) {
                    yield new BoolValue(false);
                } else {
                    throw new RuntimeError("Invalid BOOL literal: '" + lexeme + "'. Must be exactly \"TRUE\" or \"FALSE\" in uppercase.");
                }
            }
            case CHAR_LITERAL -> {
                // Strip the single quotes from the character literal (e.g., 'c')
                if (lexeme.length() >= 3) yield new CharValue(lexeme.charAt(1));
                yield new CharValue(lexeme.charAt(0));
            }
            case STRING_LITERAL -> {
                // Strip the leading and trailing quotation marks
                String rawString = lexeme.substring(1, lexeme.length() - 1);
                yield new StringValue(rawString);
            }
            case ESCAPE_LITERAL -> {
                // lexeme is 3 characters long (e.g., "[[]", "[]]", "[n]", "[#]")
                char inner = lexeme.charAt(1);
                char evaluated = switch (inner) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    default -> inner; // If it's '[', ']', '$', '&', '#', it returns exactly that character!
                };
                yield new CharValue(evaluated);
            }
            default -> throw new RuntimeError("Unrecognized literal format: " + node.valueToken.type);
        };
    }

    @Override
    public RuntimeValue visitIdentifierNode(IdentifierNode node) {
        return environment.get(node.name.lexeme);
    }

    @Override
    public RuntimeValue visitBinaryExprNode(BinaryExprNode node) {

        // TODO (DONE by She!): Add EQUAL_EQUAL and NOT_EQUAL

        RuntimeValue left = node.left.accept(this);
        RuntimeValue right = node.right.accept(this);

        if (node.operator.type == TokenType.AMPERSAND) {
            String result = left.asString() + right.asString();
            return new StringValue(result);
        }

        // Equality/inequality can compare booleans or chars directly
        if (node.operator.type == TokenType.EQUAL_EQUAL) {
            return new BoolValue(lexorEquals(left, right));
        }
        if (node.operator.type == TokenType.NOT_EQUAL) {
            return new BoolValue(!lexorEquals(left, right));
        }

        // Numeric operations only from here
        boolean isFloat = (left instanceof FloatValue || right instanceof FloatValue);
        float leftF, rightF;
        try {
            leftF = Float.parseFloat(left.asString());
            rightF = Float.parseFloat(right.asString());
        } catch (NumberFormatException e) {
            throw new RuntimeError("Arithmetic operator '" + node.operator.lexeme +
                    "' cannot be applied to non-numeric values.");
        }

        return switch (node.operator.type) {
            case PLUS  -> isFloat ? new FloatValue(leftF + rightF) : new IntValue((int)(leftF + rightF));
            case MINUS -> isFloat ? new FloatValue(leftF - rightF) : new IntValue((int)(leftF - rightF));
            case STAR  -> isFloat ? new FloatValue(leftF * rightF) : new IntValue((int)(leftF * rightF));
            case SLASH -> {
                if (rightF == 0) throw new RuntimeError("Division by zero.");
                yield isFloat ? new FloatValue(leftF / rightF) : new IntValue((int)(leftF / rightF));
            }
            case MODULO -> isFloat ? new FloatValue(leftF % rightF) : new IntValue((int)(leftF % rightF));
            case GREATER       -> new BoolValue(leftF > rightF);
            case LESS          -> new BoolValue(leftF < rightF);
            case GREATER_EQUAL -> new BoolValue(leftF >= rightF);
            case LESS_EQUAL    -> new BoolValue(leftF <= rightF);
            default -> throw new RuntimeError("Unknown binary operator: " + node.operator.lexeme);
        };
    }

    @Override
    public RuntimeValue visitLogicalExprNode(LogicalExprNode node) {
        // TODO: Use isTruthy() instead of raw cast for safe, consistent boolean evaluation.
        boolean leftVal  = isTruthy(node.left.accept(this));
        boolean rightVal = isTruthy(node.right.accept(this));

        return switch (node.operator.type) {
            case AND -> new BoolValue(leftVal && rightVal);
            case OR  -> new BoolValue(leftVal || rightVal);
            default  -> throw new RuntimeError("Unknown logical operator: " + node.operator.lexeme);
        };
    }

    @Override
    public RuntimeValue visitUnaryExprNode(UnaryExprNode node) {
        RuntimeValue right = node.right.accept(this);

        if (node.operator.type == TokenType.NOT) {
            return new BoolValue(!isTruthy(right));
        }

        float value = Float.parseFloat(right.asString());
        boolean isFloat = right instanceof FloatValue;

        if (node.operator.type == TokenType.UNARY_MINUS) {
            return isFloat ? new FloatValue(-value) : new IntValue((int) -value);
        }

        return right; // Handles PLUS (no-op)
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

    private boolean lexorEquals(RuntimeValue left, RuntimeValue right) {
        // Both numeric: promote to float for comparison
        if ((left instanceof IntValue || left instanceof FloatValue) &&
                (right instanceof IntValue || right instanceof FloatValue)) {
            float l = Float.parseFloat(left.asString());
            float r = Float.parseFloat(right.asString());
            return l == r;
        }
        // Same type: use normal equals
        return left.getValue().equals(right.getValue());
    }
}