package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

public class NewlineNode extends ExpressionNode{

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitNewlineNode(this);
    }
}
