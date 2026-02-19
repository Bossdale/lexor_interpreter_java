package org.lexor.ast.nodes;

import org.lexor.lexer.Token;
import org.lexor.ast.visitor.ASTVisitor;

public class IdentifierNode extends ExpressionNode {
    public final Token name;

    public IdentifierNode(Token name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifierNode(this);
    }
}