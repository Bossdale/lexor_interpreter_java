package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

// A statement performs an action but doesn't return a value
public abstract class StatementNode implements ASTNode {

    public abstract <T> T accept(ASTVisitor<T> visitor);
}
