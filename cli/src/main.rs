use clap::{Parser, Subcommand};
use std::process::Command;
use std::path::Path;
use colored::*;

#[derive(Parser)]
#[command(author, version, about, long_about = None)]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// Compile and run an Epsilon script
    Run {
        /// Path to the .epsilon file
        file: String,
    },
    /// Build all components of the ecosystem
    Build,
}

fn main() {
    let cli = Cli::parse();

    match &cli.command {
        Commands::Run { file } => {
            println!("{} Compiling {}...", "[Epsilon]".bold().cyan(), file.green());
            
            // 1. Run Java Compiler
            let status = Command::new("java")
                .args(&["-cp", "../frontend/bin", "com.epsilon.frontend.Main", file])
                .status()
                .expect("Failed to execute Java frontend compiler");

            if !status.success() {
                eprintln!("{} Compilation failed.", "Error:".bold().red());
                return;
            }
            
            println!("{} Compilation successful.", "[Epsilon]".bold().cyan());
            
            // 2. Run C VM
            let vm_path = if cfg!(windows) { "../vm/evm.exe" } else { "../vm/evm" };
            
            if !Path::new(vm_path).exists() {
                 eprintln!("{} VM Executable not found. Run 'epsilon build' first.", "Error:".bold().red());
                 return;
            }

            println!("{} Launching VM...", "[Epsilon]".bold().cyan());
            let vm_status = Command::new(vm_path)
                .arg("program.evm")
                .status()
                .expect("Failed to execute VM");
                
            if !vm_status.success() {
                eprintln!("{} VM reported an error.", "Error:".bold().red());
            }
        }
        Commands::Build => {
            println!("{} Orchestrating build...", "[Epsilon]".bold().cyan());
            let build_status = Command::new("make")
                .arg("all")
                .status()
                .expect("Failed to run make. Ensure 'make' is installed.");
            
            if build_status.success() {
                println!("{} All components built successfully.", "Success:".bold().green());
            }
        }
    }
}
