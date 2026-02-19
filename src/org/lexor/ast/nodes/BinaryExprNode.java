package org.lexor.ast.nodes;

import org.lexor.lexer.Token;
import org.lexor.ast.visitor.ASTVisitor;

public class BinaryExprNode extends ExpressionNode {
    public final ExpressionNode left;
    public final Token operator;      // +, -, *, /, %, &
    public final ExpressionNode right;

    public BinaryExprNode(ExpressionNode left, Token operator, ExpressionNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryExprNode(this);
    }
}