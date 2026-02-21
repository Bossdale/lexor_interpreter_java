package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

public class ForNode extends StatementNode{
    public final StatementNode initialization;
    public final ExpressionNode condition;
    public final StatementNode update;
    public final BlockNode body;

    public ForNode(StatementNode initialization, ExpressionNode condition, StatementNode update, BlockNode body) {
        this.initialization = initialization;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }


    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitForNode(this);
    }
}
