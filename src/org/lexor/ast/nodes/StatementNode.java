package org.lexor.ast.nodes;
import org.lexor.ast.visitor.ASTVisitor;

// TODO: Add visitStatementNode to ASTVisitor and implement it
//  StatementNode is an abstract base class, and ASTVisitor has no visitStatementNode.
//  This is fine architecturally — but it means the ASTPrinter and Interpreter cannot dispatch a raw StatementNode cleanly if one ever surfaces.
//  Add a default guard.

// TODO: StatementNode is the abstract base for all executable statements.
//       It should remain abstract and never be instantiated directly.
//       The accept() method forces all subclasses to register with the visitor.

public abstract class StatementNode implements ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);
}
