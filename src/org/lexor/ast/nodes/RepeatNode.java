package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

public class RepeatNode extends StatementNode{
    public final ExpressionNode condition;
    public final BlockNode body;


    public RepeatNode(ExpressionNode condition, BlockNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitRepeatNode(this);
    }
}
