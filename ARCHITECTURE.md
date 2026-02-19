# LEXOR Interpreter Project Structure

This document outlines the architectural structure of the LEXOR Interpreter, built in Java. The project is organized into distinct phases of the compilation/interpretation pipeline to ensure scalability, separation of concerns, and maintainability.

## Directory Tree

```text
src/
└── org/
    └── lexor/
        ├── Main.java                 # The main entry point. Orchestrates file reading, lexing, parsing, and execution.
        │
        ├── lexer/                    # PHASE 1: Lexical Analysis
        │                             # Converts raw source code string into a stream of meaningful Tokens.
        │
        ├── parser/                   # PHASE 2: Syntax Analysis
        │                             # Consumes Tokens and builds the Abstract Syntax Tree (AST) according to LEXOR's grammar.
        │
        ├── ast/                      # Abstract Syntax Tree Definitions
        │   ├── nodes/                # Pure data classes representing different constructs (e.g., VarDeclNode, BinaryExprNode).
        │   └── visitor/              # Implements the Visitor pattern to separate operations (like interpretation) from the AST data nodes.
        │
        ├── semantic/                 # PHASE 3: Semantic Analysis
        │   │                         # Checks rules that the parser cannot, such as strong typing and declaration order.
        │   ├── SemanticAnalyzer.java # Enforces rules (e.g., variables must be declared right after START SCRIPT).
        │   └── symbol/               # Manages state before runtime to ensure logical correctness.
        │       ├── SymbolTable.java  # Tracks declared variables, their scope, and their assigned types.
        │       ├── Symbol.java       # Represents a single declared identifier.
        │       └── Type.java         # Defines the core LEXOR types (INT, CHAR, BOOL, FLOAT).
        │
        ├── runtime/                  # PHASE 4: Execution
        │   │                         # The actual evaluation of the validated AST.
        │   ├── Interpreter.java      # Visits AST nodes and executes their corresponding behavior.
        │   ├── Environment.java      # The runtime memory. Stores variables and their current values during execution.
        │   └── values/               # Wrappers for LEXOR's native data types to handle them safely in Java.
        │       ├── RuntimeValue.java # Base interface/abstract class for all LEXOR values.
        │       ├── IntValue.java     # Represents a 4-byte INT.
        │       ├── BoolValue.java    # Represents a BOOL (true/false).
        │       ├── FloatValue.java   # Represents a 4-byte FLOAT.
        │       └── CharValue.java    # Represents a CHAR (single symbol).
        │
        └── error/                    # Global Error Handling
                                      # Contains custom exception classes for Lexical, Syntax, and Runtime errors.