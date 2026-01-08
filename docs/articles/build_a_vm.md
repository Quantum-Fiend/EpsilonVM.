# Building a Multi-Language Compiler Ecosystem from Scratch

*By Tushar*

Building a compiler is often seen as a dark art. Building one that spans 6 different programming languages? That’s an engineering challenge. In this article, I break down how I built **EpsilonVM**, a custom language ecosystem featuring a Java Frontend, C VM, C++ JIT, and Rust Toolchain.

## The Architecture
Most language resources focus on a single pipeline (e.g. C++ only). EpsilonVM takes a polyglot approach:
1.  **Frontend (Java)**: Leveraging Java's strong OOP for AST handling.
2.  **Runtime (C)**: Using C for the Interpreter ensures zero-overhead execution and manual memory layout control.
3.  **JIT (C++)**: C++ was chosen for the JIT module to easily interface with LLVM (future plan) or emit raw machine code buffers using `mmap`.

## The Bytecode Design
I opted for a **Register-based VM**, similar to Lua 5.0 and Android's ART.
Stack-based VMs (like JVM) are simpler to compile to, but they generate more instructions (`PUSH`, `POP`, `ADD`). Register VMs generate `ADD r1, r2, r3` - much closer to the CPU.

## Implementing the Garbage Collector
The scariest part of any VM is memory management. I implemented a classic **Mark-Sweep GC**. It forces you to understand the exact object graph of your running program. If you miss one pointer in the root set, your program segfaults.

## Conclusion
Systems programming forces you to understand the computer at a deeper level. EpsilonVM is my proof-of-concept that building a language is accessible if you break it down into modular components.

[Check out the Code on GitHub]
