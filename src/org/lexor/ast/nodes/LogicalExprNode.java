package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;
import org.lexor.lexer.Token;

public class LogicalExprNode extends ExpressionNode{
    public final ExpressionNode left;
    public final Token operator;
    public final ExpressionNode right;

    public LogicalExprNode(ExpressionNode left, Token operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLogicalExprNode(this);
    }
}
