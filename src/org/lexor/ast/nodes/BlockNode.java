package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;
import java.util.List;
public class BlockNode extends StatementNode{
    public final List<StatementNode> statements;

    public BlockNode(List<StatementNode> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBlockNode(this);
    }
}
