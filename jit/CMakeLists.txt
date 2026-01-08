cmake_minimum_required(VERSION 3.10)
project(EpsilonJIT)

set(CMAKE_CXX_STANDARD 17)

include_directories(include)

add_library(jit SHARED
    src/jit.cpp
)
