package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

// An expression always evaluates to a value
public abstract class ExpressionNode implements ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);
}