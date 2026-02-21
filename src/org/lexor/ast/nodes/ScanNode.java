package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;
import org.lexor.lexer.Token;
import java.util.List;

public class ScanNode extends StatementNode{
    public final List<Token> identifiers;

    public ScanNode(List<Token> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitScanNode(this);
    }
}
