#include <stdio.h>
#include "vm.h"

int main(int argc, char* argv[]) {
    if (argc < 2) {
        printf("Usage: evm <path_to_bytecode>\n");
        return 1;
    }
    
    VM vm;
    initVM(&vm);
    
    Chunk chunk;
    loadFile(argv[1], &chunk, &vm);
    
    printf("--- Executing %s ---\n", argv[1]);
    interpret(&vm, &chunk);
    printf("--- Execution Finished ---\n");
    
    freeVM(&vm);
    return 0;
}
