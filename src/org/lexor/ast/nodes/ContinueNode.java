package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

public class ContinueNode extends StatementNode {
    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitContinueNode(this);
    }
}
