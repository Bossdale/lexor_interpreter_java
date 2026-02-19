package org.lexor.ast.nodes;

import org.lexor.lexer.Token;
import org.lexor.ast.visitor.ASTVisitor;

public class AssignmentNode extends StatementNode {
    public final Token identifier;
    public final ExpressionNode value;

    public AssignmentNode(Token identifier, ExpressionNode value) {
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitAssignmentNode(this);
    }
}