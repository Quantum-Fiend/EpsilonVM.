#ifndef JIT_H
#define JIT_H

#include <vector>
#include <cstdint>

// Forward declaration for VM structs if we were linking directly,
// but usually JIT takes raw bytecode or IR.
// We'll use a simplified interface.

namespace Epsilon {

struct JITConfig {
    bool optimize;
    int threshold;
};

class JITCompiler {
public:
    JITCompiler(JITConfig config);
    ~JITCompiler();

    // Compiles a sequence of bytecode instructions into native machine code
    // Returns a function pointer to the generated code
    typedef int (*NativeFunc)(void* regs); // regs is pointer to register array
    
    NativeFunc compile(const std::vector<uint32_t>& bytecode);

private:
    void* allocateExecutableMemory(size_t size);
    void emitPrologue(std::vector<uint8_t>& code);
    void emitEpilogue(std::vector<uint8_t>& code);
    
    // x86-64 specific emitters
    void emitAdd(std::vector<uint8_t>& code, uint8_t rA, uint8_t rB, uint8_t rC);
    void emitSub(std::vector<uint8_t>& code, uint8_t rA, uint8_t rB, uint8_t rC);
    void emitMul(std::vector<uint8_t>& code, uint8_t rA, uint8_t rB, uint8_t rC);
    void emitLoadK(std::vector<uint8_t>& code, uint8_t dst, int64_t val);
    void emitRet(std::vector<uint8_t>& code);
    
    JITConfig config;
};

} // namespace Epsilon

#endif // JIT_H
