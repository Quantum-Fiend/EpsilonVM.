# Epsilon Language Specification

## 1. Syntax & Grammar
Epsilon uses a C-style syntax with dynamic typing.

### Variables
Declared using `var` keyword.
```epsilon
var x = 10;
var name = "Epsilon";
```

### Control Flow
Standard `if`, `else`, `while` constructs.
```epsilon
if (x > 5) {
  print "Big";
} else {
  print "Small";
}

while (x > 0) {
  x = x - 1;
}
```

### Functions
Declared using `fn`. First-class support planned.
```epsilon
fn add(a, b) {
  return a + b;
}
```

## 2. Types
- `Int`: 64-bit signed integer.
- `Float`: 64-bit floating point.
- `String`: UTF-8 immutable string.
- `Object`: Host for complex types (Structs, Arrays - planned).

## 3. Standard Library
- `print <expr>`: Outputs value to stdout.
- `clock()`: Returns current high-resolution time in seconds.
