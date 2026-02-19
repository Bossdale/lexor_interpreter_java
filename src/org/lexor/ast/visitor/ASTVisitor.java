package org.lexor.ast.visitor;

import org.lexor.ast.nodes.*;

public interface ASTVisitor<T> {
    T visitProgramNode(ProgramNode node);
    T visitVarDeclNode(VarDeclNode node);
    T visitAssignmentNode(AssignmentNode node);
    T visitBinaryExprNode(BinaryExprNode node);
    T visitLiteralNode(LiteralNode node);
    T visitIdentifierNode(IdentifierNode node);
}