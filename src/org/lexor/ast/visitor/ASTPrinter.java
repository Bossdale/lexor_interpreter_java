package org.lexor.ast.visitor;

import org.lexor.ast.nodes.*;

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
        return node.valueToken.lexeme;
    }

    @Override
    public String visitIdentifierNode(IdentifierNode node) {
        return node.name.lexeme;
    }
}