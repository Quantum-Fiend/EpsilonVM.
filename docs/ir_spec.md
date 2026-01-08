# EpsilonVM Intermediate Representation (IR) Spec

The frontend (Java) will produce a `.eir` (Epsilon IR) file, which can be:
1. Directly compiled to Bytecode.
2. Visualized by the Python tool.
3. Consumed by JIT for analysis.

## Structure
The IR is tree-based (AST) but can be flattened. We will use a JSON-based format for simple interoperability between Java, C++, Python, and Rust.

## JSON Schema

```json
{
  "program": "MyScript",
  "functions": [
    {
      "name": "main",
      "args": [],
      "body": [
        { "type": "VarDecl", "name": "x", "value": { "type": "IntLiteral", "val": 10 } },
        { "type": "Assign", "target": "x", "value": { "type": "BinaryOp", "op": "+", "left": "x", "right": 1 } },
        { "type": "Print", "value": "x" }
      ]
    }
  ]
}
```

## Node Types

- `Program`: Root node.
- `Function`: Definition.
- `Block`: Sequence of statements.
- `VarDecl`: Variable declaration.
- `Assign`: Assignment.
- `BinaryOp`: `+, -, *, /, ==, !=, <`
- `If`: Conditional.
- `While`: Loop.
- `Call`: Function call.
- `Return`: Return statement.
- `Literal`: Int, Float, String, Boolean.
