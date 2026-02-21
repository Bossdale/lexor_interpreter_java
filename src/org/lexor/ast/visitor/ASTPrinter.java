package org.lexor.ast.visitor;

import org.lexor.ast.nodes.*;
import java.util.List;

public class ASTPrinter implements ASTVisitor<String> {

    public String print(ASTNode node) {
        return node.accept(this);
    }

    @Override
    public String visitProgramNode(ProgramNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("AST_ROOT:\n");

        builder.append("  DECLARATIONS:\n");
        for (VarDeclNode decl : node.declarations) {
            builder.append("    ").append(decl.accept(this)).append("\n");
        }

        builder.append("  STATEMENTS:\n");
        for (StatementNode stmt : node.statements) {
            builder.append("    ").append(stmt.accept(this)).append("\n");
        }
        return builder.toString();
    }

    @Override
    public String visitVarDeclNode(VarDeclNode node) {
        String initStr = (node.initializer != null) ? node.initializer.accept(this) : "null";
        return String.format("(Declare %s %s = %s)",
                node.dataType.lexeme, node.identifier.lexeme, initStr);
    }

    @Override
    public String visitAssignmentNode(AssignmentNode node) {
        return String.format("(Assign %s = %s)",
                node.identifier.lexeme, node.value.accept(this));
    }

    @Override
    public String visitBinaryExprNode(BinaryExprNode node) {
        return String.format("(%s %s %s)",
                node.operator.lexeme, node.left.accept(this), node.right.accept(this));
    }

    @Override
    public String visitLiteralNode(LiteralNode node) {
        return String.format("(%s: %s)",
                node.valueToken.type,
                node.valueToken.lexeme);
    }

    @Override
    public String visitIdentifierNode(IdentifierNode node) {
        return String.format("(Id: %s)",
                node.name.lexeme);
    }

    @Override
    public String visitPrintNode(PrintNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("(Print ");

        List<ExpressionNode> expressions = node.getExpressions();

        for (int i = 0; i < expressions.size(); i++) {
            builder.append(expressions.get(i).accept(this));

            if (i < expressions.size() - 1) {
                builder.append(" & ");
            }
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitUnaryExprNode(UnaryExprNode node) {
        return String.format("(%s %s)",
                node.operator.lexeme,
                node.right.accept(this));
    }

    @Override
    public String visitBlockNode(BlockNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("(Block");

        for (StatementNode stmt : node.statements) {
            builder.append(" ").append(stmt.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitGroupingNode(GroupingNode node) {
        return String.format("(group %s)", node.expression.accept(this));
    }

    @Override
    public String visitIfNode(IfNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("(If ").append(node.condition.accept(this));
        builder.append(" Then ").append(node.thenBranch.accept(this));

        for(IfNode.ElseIfPart part : node.elseIfParts){
            builder.append("ElseIf").append(part.condition.accept(this));
            builder.append(" ").append(part.body.accept(this));
        }
        if (node.elseBranch != null) {
            builder.append(" Else ").append(node.elseBranch.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitRepeatNode(RepeatNode node) {
        return String.format("(Repeat %s %s)",
                node.condition.accept(this),
                node.body.accept(this));
    }

    @Override
    public String visitScanNode(ScanNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("(Scan ");

        for (int i = 0; i < node.identifiers.size(); i++) {
            builder.append(node.identifiers.get(i).lexeme);

            if (i < node.identifiers.size() - 1) {
                builder.append(", ");
            }
        }

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitForNode(ForNode node) {
        StringBuilder builder = new StringBuilder();
        builder.append("(For ");

        builder.append("Init: ").append(node.initialization.accept(this));
        builder.append(" Condition: ").append(node.condition.accept(this));
        builder.append(" Update: ").append(node.update.accept(this));
        builder.append(" Body: ").append(node.body.accept(this));

        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitLogicalExprNode(LogicalExprNode node) {
        return String.format("(%s %s %s)",
                node.operator.lexeme,
                node.left.accept(this),
                node.right.accept(this));
    }

    @Override
    public String visitNewlineNode(NewlineNode node) {
        return "$";
    }
}