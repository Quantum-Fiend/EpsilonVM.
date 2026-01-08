# Contributing to EpsilonVM

Thank you for your interest in EpsilonVM! Being a polyglot ecosystem, contributing requires awareness of multiple language toolchains.

## 🛠️ Environment Setup

Ensure you have the following installed:
- **Java JDK 11+** (Frontend)
- **CMake & GCC/Clang** (VM Core & JIT)
- **Rust (Cargo)** (CLI Driver)
- **Python 3.8+** (Visualizer & Tests)
- **Node.js** (LSP)

## 🏗️ Build Process

The project uses a root `Makefile` to orchestrate builds across all languages.

```bash
# Build everything (Frontend, VM, JIT, CLI)
make all

# Build specific components
make frontend
make vm
make jit
make cli
```

## 🧪 Testing

We use a Python-based integration suite for end-to-end verification.

```bash
make test
```

Please ensure all tests pass before submitting a Pull Request.

## 📜 Coding Standards

- **C/C++**: Follow the existing style (2-space indent, `snake_case` for C, `PascalCase` for C++ classes).
- **Java**: Follow standard Java conventions (`camelCase`).
- **Rust**: Use `cargo fmt` before committing.
- **Python**: Follow PEP 8.

## 🚀 Feature Requests

We are currently interested in:
- New language frontends (e.g., Lisp, Scheme).
- Advanced JIT optimizations (Register allocation).
- Extended Native FFI for more system-level interactions.

---
*Maintained by the Epsilon Core Team*
