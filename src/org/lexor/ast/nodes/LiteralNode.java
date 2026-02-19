package org.lexor.ast.nodes;

import org.lexor.lexer.Token;
import org.lexor.ast.visitor.ASTVisitor;

public class LiteralNode extends ExpressionNode {
    public final Token valueToken; // e.g., an INT_LITERAL or BOOL_LITERAL token

    public LiteralNode(Token valueToken) {
        this.valueToken = valueToken;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLiteralNode(this);
    }
}