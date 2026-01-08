# EpsilonVM Bytecode Specification (v1.0)

This document defines the instruction set architecture (ISA) and binary file format for EpsilonVM.

## 1. Data Types
- **Int**: 64-bit signed integer (`i64`)
- **Float**: 64-bit floating point (`f64`)
- **String**: UTF-8 encoded string (reference)
- **Object**: Reference to a heap object (Array, Struct)

## 2. Virtual Machine Model
- **Type**: Register-based.
- **Registers**: `r0` to `r255` (256 virtual registers per stack frame).

## 3. Instruction Set (Opcodes)

| Opcode | Mnemonic | Operands | Description |
| :--- | :--- | :--- | :--- |
| 0x00 | `HALT` | - | Stop execution |
| 0x01 | `LOADk`| `rA, k` | Load constant at index `k` into register `rA` |
| 0x02 | `LOAD` | `rA, rB` | Copy value from `rB` to `rA` |
| 0x10 | `ADD` | `rA, rB, rC` | `rA = rB + rC` |
| 0x11 | `SUB` | `rA, rB, rC` | `rA = rB - rC` |
| 0x12 | `MUL` | `rA, rB, rC` | `rA = rB * rC` |
| 0x13 | `DIV` | `rA, rB, rC` | `rA = rB / rC` |
| 0x20 | `JMP` | `offset` | Jump relative by `offset` (signed 16-bit) |
| 0x21 | `JMP_IF`| `rA, offset` | Jump by `offset` if `rA` is true (non-zero) |
| 0x30 | `CALL` | `func_idx, nArgs` | Call function at `func_idx` with `nArgs` |
| 0x31 | `RET` | `rA` | Return value in `rA` |
| 0x40 | `PRINT`| `rA` | Print value in `rA` |

## 4. Binary File Format (.evm)

```
[Header]
Magic Number: 0x45 0x56 0x4D 0x00 ("EVM\0")
Version: 1

[Constant Pool]
Count: u32
Const 0: Type(u8) Data(...)

[Functions]
Count: u32
  [Function 0]
  Name Index: u32
  Arg Count: u8
  Register Count: u8
  Instructions: [u32, ...]
```
