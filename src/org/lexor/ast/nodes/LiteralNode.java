package org.lexor.ast.nodes;

import org.lexor.lexer.Token;
import org.lexor.ast.visitor.ASTVisitor;

public class LiteralNode extends ExpressionNode {
    public final Token valueToken;

    public LiteralNode(Token valueToken) {
        this.valueToken = valueToken;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteralNode(this);
    }
}