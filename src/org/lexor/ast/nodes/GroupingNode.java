package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

public class GroupingNode extends ExpressionNode{
    public final ExpressionNode expression;

    public GroupingNode(ExpressionNode expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitGroupingNode(this);
    }
}
