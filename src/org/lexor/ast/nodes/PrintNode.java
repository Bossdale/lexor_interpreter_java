package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;
import java.util.List;
public class PrintNode extends StatementNode{
    public final List<ExpressionNode> expressions;

    public PrintNode(List<ExpressionNode> expressions) {
        this.expressions = expressions;
    }

    public List<ExpressionNode> getExpressions() {
        return expressions;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitPrintNode(this);
    }
}
