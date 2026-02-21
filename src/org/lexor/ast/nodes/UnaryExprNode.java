package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;
import org.lexor.lexer.Token;

public class UnaryExprNode extends ExpressionNode{
    public final Token operator;
    public final ExpressionNode right;

    public UnaryExprNode(Token operator, ExpressionNode right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitUnaryExprNode(this);
    }
}
