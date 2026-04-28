package org.lexor.ast.nodes;
import org.lexor.ast.visitor.ASTVisitor;

public abstract class StatementNode implements ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);
}
