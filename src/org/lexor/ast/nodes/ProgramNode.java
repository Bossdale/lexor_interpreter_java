package org.lexor.ast.nodes;

import org.lexor.ast.visitor.ASTVisitor;
import java.util.List;

public class ProgramNode extends StatementNode {
    public final List<VarDeclNode> declarations;
    public final List<StatementNode> statements;

    public ProgramNode(List<VarDeclNode> declarations, List<StatementNode> statements) {
        this.declarations = declarations;
        this.statements = statements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitProgramNode(this);
    }
}