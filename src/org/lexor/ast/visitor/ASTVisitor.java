package org.lexor.ast.visitor;

import org.lexor.ast.nodes.*;

public interface ASTVisitor<T> {
    T visitProgramNode(ProgramNode node);
    T visitVarDeclNode(VarDeclNode node);
    T visitAssignmentNode(AssignmentNode node);
    T visitBinaryExprNode(BinaryExprNode node);
    T visitLiteralNode(LiteralNode node);
    T visitIdentifierNode(IdentifierNode node);
    T visitPrintNode(PrintNode node);
    T visitUnaryExprNode(UnaryExprNode node);
    T visitBlockNode(BlockNode node);
    T visitGroupingNode(GroupingNode node);
    T visitIfNode(IfNode node);
    T visitRepeatNode(RepeatNode node);
    T visitScanNode(ScanNode node);
    T visitForNode(ForNode node);
    T visitLogicalExprNode(LogicalExprNode node);
    T visitNewlineNode(NewlineNode node);
}