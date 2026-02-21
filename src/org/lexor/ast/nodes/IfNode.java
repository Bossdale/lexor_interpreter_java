package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

import java.util.List;

public class IfNode extends StatementNode{
    public final ExpressionNode condition;
    public final StatementNode thenBranch;
    public final List<ElseIfPart> elseIfParts;
    public final StatementNode elseBranch;

    public IfNode(ExpressionNode condition, StatementNode thenBranch, List<ElseIfPart> elseIfParts, StatementNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseIfParts = elseIfParts;
        this.elseBranch = elseBranch;
    }

    public static class ElseIfPart {
        public final ExpressionNode condition;
        public final BlockNode body;
        public ElseIfPart(ExpressionNode condition, BlockNode body) {
            this.condition = condition;
            this.body = body;
        }
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIfNode(this);
    }
}
