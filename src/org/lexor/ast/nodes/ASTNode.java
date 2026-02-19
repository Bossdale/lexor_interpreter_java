package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;

public interface ASTNode {
    <T> T accept(ASTVisitor<T> visitor);
}