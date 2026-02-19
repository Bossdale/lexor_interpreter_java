package org.lexor.ast.nodes;

import org.lexor.lexer.Token;
import org.lexor.ast.visitor.ASTVisitor;

public class VarDeclNode extends StatementNode {
    public final Token dataType;      // INT, FLOAT, CHAR, BOOL
    public final Token identifier;    // The variable name
    public final ExpressionNode initializer; // The value assigned (can be null if uninitialized)

    public VarDeclNode(Token dataType, Token identifier, ExpressionNode initializer) {
        this.dataType = dataType;
        this.identifier = identifier;
        this.initializer = initializer;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitVarDeclNode(this);
    }
}