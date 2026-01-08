# EpsilonVM Root Orchestrator
# unifies builds for Java (Frontend), C (VM), C++ (JIT), Rust (CLI), TS (LSP)

.PHONY: all frontend vm jit cli lsp clean test

all: frontend vm jit cli

frontend:
	@echo "[Building Frontend (Java)...]"
	cd frontend && javac -d bin src/main/java/com/epsilon/frontend/*.java

vm:
	@echo "[Building VM Core (C)...]"
	cd vm && cmake . && cmake --build .

jit:
	@echo "[Building JIT (C++)...]"
	cd jit && cmake . && cmake --build .

cli:
	@echo "[Building CLI (Rust)...]"
	cd cli && cargo build --release

lsp:
	@echo "[Installing Dependencies for LSP (JS)...]"
	cd lsp && npm install

test: all
	@echo "[Running Integration Tests (Python)...]"
	python tests/run_tests.py

clean:
	@echo "[Cleaning all components...]"
	rm -rf frontend/bin/*
	rm -rf vm/CMakeFiles vm/CMakeCache.txt vm/Makefile vm/evm*
	rm -rf jit/CMakeFiles jit/CMakeCache.txt jit/Makefile jit/libjit*
	cd cli && cargo clean
	cd lsp && rm -rf out node_modules
