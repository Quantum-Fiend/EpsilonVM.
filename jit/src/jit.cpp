#include "jit.h"
#include <iostream>
#include <sys/mman.h> // POSIX (Unistd) - might need Windows equivalent logic
#include <cstring>

#ifdef _WIN32
#include <windows.h>
#endif

namespace Epsilon {

JITCompiler::JITCompiler(JITConfig config) : config(config) {}

JITCompiler::~JITCompiler() {
    // Should track and free allocated pages
}

void* JITCompiler::allocateExecutableMemory(size_t size) {
#ifdef _WIN32
    void* ptr = VirtualAlloc(NULL, size, MEM_COMMIT | MEM_RESERVE, PAGE_EXECUTE_READWRITE);
    if (!ptr) {
        std::cerr << "Failed to allocate executable memory" << std::endl;
        return nullptr;
    }
    return ptr;
#else
    void* ptr = mmap(NULL, size, PROT_READ | PROT_WRITE | PROT_EXEC,
                     MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (ptr == MAP_FAILED) {
        perror("mmap");
        return nullptr;
    }
    return ptr;
#endif
}

JITCompiler::NativeFunc JITCompiler::compile(const std::vector<uint32_t>& bytecode) {
    std::vector<uint8_t> mc; // Machine Code buffer

    emitPrologue(mc);

    for (uint32_t instr : bytecode) {
        uint8_t op = (instr >> 24) & 0xFF;
        uint8_t rA = (instr >> 16) & 0xFF;
        uint8_t rB = (instr >> 8) & 0xFF;
        uint8_t rC = instr & 0xFF;

        switch (op) {
            case 0x01: // LOADK
                // Simplification for demo: assuming loading constant value 42
                // mov rA, 42
                // In reality we need the constant pool
                break;
            case 0x10: // ADD
                emitAdd(mc, rA, rB, rC);
                break;
            case 0x11: // SUB
                emitSub(mc, rA, rB, rC);
                break;
            case 0x12: // MUL
                emitMul(mc, rA, rB, rC);
                break;
            case 0x31: // RET
                emitEpilogue(mc);
                emitRet(mc);
                break;
            default:
                // Fallback to interpreter?
                break;
        }
    }
    
    // Finalize
    void* mem = allocateExecutableMemory(mc.size());
    if (mem) {
        memcpy(mem, mc.data(), mc.size());
        return (NativeFunc)mem;
    }
    return nullptr;
}

void JITCompiler::emitPrologue(std::vector<uint8_t>& code) {
    // push rbp
    code.push_back(0x55);
    // mov rbp, rsp
    code.push_back(0x48);
    code.push_back(0x89);
    code.push_back(0xE5);
}

void JITCompiler::emitEpilogue(std::vector<uint8_t>& code) {
    // pop rbp
    code.push_back(0x5D);
}

void JITCompiler::emitRet(std::vector<uint8_t>& code) {
    // ret
    code.push_back(0xC3);
}

void JITCompiler::emitAdd(std::vector<uint8_t>& code, uint8_t rA, uint8_t rB, uint8_t rC) {
    // mov rax, [rdi + rB*8]
    // add rax, [rdi + rC*8]
    // mov [rdi + rA*8], rax
    // (Assuming rdi is the first argument 'regs')
    
    // mov rax, [rdi + rB*8]
    code.push_back(0x48); code.push_back(0x8B); 
    code.push_back(0x87); // ModRM
    uint32_t offsetB = rB * 8;
    code.push_back(offsetB & 0xFF); code.push_back((offsetB >> 8) & 0xFF);
    code.push_back((offsetB >> 16) & 0xFF); code.push_back((offsetB >> 24) & 0xFF);

    // add rax, [rdi + rC*8]
    code.push_back(0x48); code.push_back(0x03);
    code.push_back(0x87); 
    uint32_t offsetC = rC * 8;
    code.push_back(offsetC & 0xFF); code.push_back((offsetC >> 8) & 0xFF);
    code.push_back((offsetC >> 16) & 0xFF); code.push_back((offsetC >> 24) & 0xFF);

    // mov [rdi + rA*8], rax
    code.push_back(0x48); code.push_back(0x89);
    code.push_back(0x87);
    uint32_t offsetA = rA * 8;
    code.push_back(offsetA & 0xFF); code.push_back((offsetA >> 8) & 0xFF);
    code.push_back((offsetA >> 16) & 0xFF); code.push_back((offsetA >> 24) & 0xFF);
}

void JITCompiler::emitSub(std::vector<uint8_t>& code, uint8_t rA, uint8_t rB, uint8_t rC) {
    // Similar to Add but use 'sub' opcode (0x2B)
    code.push_back(0x48); code.push_back(0x8B); code.push_back(0x87);
    uint32_t offsetB = rB * 8;
    code.push_back(offsetB & 0xFF); code.push_back((offsetB >> 8) & 0xFF);
    code.push_back((offsetB >> 16) & 0xFF); code.push_back((offsetB >> 24) & 0xFF);

    code.push_back(0x48); code.push_back(0x2B); code.push_back(0x87);
    uint32_t offsetC = rC * 8;
    code.push_back(offsetC & 0xFF); code.push_back((offsetC >> 8) & 0xFF);
    code.push_back((offsetC >> 16) & 0xFF); code.push_back((offsetC >> 24) & 0xFF);

    code.push_back(0x48); code.push_back(0x89); code.push_back(0x87);
    uint32_t offsetA = rA * 8;
    code.push_back(offsetA & 0xFF); code.push_back((offsetA >> 8) & 0xFF);
    code.push_back((offsetA >> 16) & 0xFF); code.push_back((offsetA >> 24) & 0xFF);
}

void JITCompiler::emitMul(std::vector<uint8_t>& code, uint8_t rA, uint8_t rB, uint8_t rC) {
    // imul rax, [rdi + rC*8]
    code.push_back(0x48); code.push_back(0x8B); code.push_back(0x87);
    uint32_t offsetB = rB * 8;
    code.push_back(offsetB & 0xFF); code.push_back((offsetB >> 8) & 0xFF);
    code.push_back((offsetB >> 16) & 0xFF); code.push_back((offsetB >> 24) & 0xFF);

    code.push_back(0x48); code.push_back(0x0F); code.push_back(0xAF); code.push_back(0x87);
    uint32_t offsetC = rC * 8;
    code.push_back(offsetC & 0xFF); code.push_back((offsetC >> 8) & 0xFF);
    code.push_back((offsetC >> 16) & 0xFF); code.push_back((offsetC >> 24) & 0xFF);

    code.push_back(0x48); code.push_back(0x89); code.push_back(0x87);
    uint32_t offsetA = rA * 8;
    code.push_back(offsetA & 0xFF); code.push_back((offsetA >> 8) & 0xFF);
    code.push_back((offsetA >> 16) & 0xFF); code.push_back((offsetA >> 24) & 0xFF);
}

} // namespace Epsilon
