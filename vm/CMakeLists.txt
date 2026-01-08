cmake_minimum_required(VERSION 3.10)
project(EpsilonVM VERSION 1.0)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED True)

include_directories(include)

add_executable(evm
    src/main.c
    src/vm.c
)
