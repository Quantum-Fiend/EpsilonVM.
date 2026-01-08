import tkinter as tk
from tkinter import ttk, filedialog
import subprocess
import os

# Premium Color Palette
COLORS = {
    "bg": "#121212",
    "sidebar": "#1e1e1e",
    "accent": "#bb86fc",
    "text": "#e0e0e0",
    "header": "#1f1f1f",
    "success": "#03dac6",
    "error": "#cf6679",
    "border": "#333333"
}

class EpsilonViz(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("EpsilonVM | Production Control Center")
        self.geometry("1400x900")
        self.configure(bg=COLORS["bg"])
        
        self.create_widgets()
        
    def create_widgets(self):
        # Header
        header = tk.Frame(self, bg=COLORS["header"], height=60, bd=0, highlightthickness=0)
        header.pack(fill=tk.X)
        
        title_frame = tk.Frame(header, bg=COLORS["header"])
        title_frame.pack(side=tk.LEFT, padx=20)
        
        tk.Label(title_frame, text="EPSILON", bg=COLORS["header"], 
                 fg=COLORS["accent"], font=("Inter", 16, "bold")).pack(side=tk.LEFT)
        tk.Label(title_frame, text=" | PRODUCTION CONTROL CENTER", bg=COLORS["header"], 
                 fg=COLORS["text"], font=("Inter", 10)).pack(side=tk.LEFT, padx=5)

        # Toolbar
        toolbar = tk.Frame(self, bg=COLORS["sidebar"], height=50, bd=1, relief=tk.FLAT)
        toolbar.pack(fill=tk.X, pady=(0, 1))
        
        self.create_tool_btn(toolbar, "LOAD SCRIPT", self.load_script, COLORS["accent"])
        self.create_tool_btn(toolbar, "COMPILE & RUN", self.run_pipeline, COLORS["success"])
        self.create_tool_btn(toolbar, "CLEAR LOGS", self.clear_logs, COLORS["error"])
        
        # Main Layout
        panes = tk.PanedWindow(self, orient=tk.HORIZONTAL, bg=COLORS["border"], bd=0, sashwidth=2)
        panes.pack(fill=tk.BOTH, expand=True)
        
        # Source Code (Left)
        left_frame = self.create_pane(panes, "SOURCE CODE")
        self.source_text = self.create_text_box(left_frame)
        self.source_text.insert("1.0", "var x = 100;\nvar y = 200;\nvar z = x + y;\nprint z;")
        
        # AST / Bytecode (Middle)
        mid_frame = self.create_pane(panes, "BYTECODE INSPECTOR")
        self.viz_text = self.create_text_box(mid_frame)
        
        # VM Output (Right)
        right_frame = self.create_pane(panes, "RUNTIME MONITOR")
        self.output_text = self.create_text_box(right_frame)

    def create_tool_btn(self, parent, text, command, color):
        btn = tk.Button(parent, text=text, command=command, bg=COLORS["sidebar"], 
                      fg=color, relief=tk.FLAT, font=("Inter", 8, "bold"),
                      activebackground=color, activeforeground="black",
                      padx=15, pady=8)
        btn.pack(side=tk.LEFT, padx=5)

    def create_pane(self, parent, title):
        frame = tk.Frame(parent, bg=COLORS["bg"], padx=10, pady=10)
        tk.Label(frame, text=title, bg=COLORS["bg"], fg=COLORS["accent"], font=("Inter", 9, "bold")).pack(anchor=tk.W, pady=(0, 5))
        parent.add(frame)
        return frame

    def create_text_box(self, parent):
        box = tk.Text(parent, bg=COLORS["sidebar"], fg=COLORS["text"], insertbackground="white",
                      font=("Consolas", 11), bd=0, padx=15, pady=15, selectbackground=COLORS["accent"])
        box.pack(fill=tk.BOTH, expand=True)
        return box

    def clear_logs(self):
        self.viz_text.delete("1.0", tk.END)
        self.output_text.delete("1.0", tk.END)

    def load_script(self):
        path = filedialog.askopenfilename(filetypes=[("Epsilon Files", "*.epsilon")])
        if path:
            with open(path, "r") as f:
                self.source_text.delete("1.0", tk.END)
                self.source_text.insert("1.0", f.read())
                
    def run_pipeline(self):
        # Determine working directory context
        # We assume we are running from EpsilonVM root or viz dir
        cwd = os.getcwd()
        if os.path.basename(cwd) == "viz":
            frontend_cp = "../frontend/bin"
            vm_exe = "../vm/evm.exe" if os.name == 'nt' else "../vm/evm"
        else:
            frontend_cp = "frontend/bin"
            vm_exe = "vm/evm.exe" if os.name == 'nt' else "vm/evm"

        temp_file = "temp.epsilon"
        with open(temp_file, "w") as f:
            f.write(self.source_text.get("1.0", tk.END))
            
        try:
             # 1. Compile
             cmd = ["java", "-cp", frontend_cp, "com.epsilon.frontend.Main", temp_file]
             result = subprocess.run(cmd, capture_output=True, text=True)
             
             self.viz_text.delete("1.0", tk.END)
             if result.returncode != 0:
                 self.viz_text.insert(tk.END, "COMPILATION FAILED:\n\n" + result.stderr)
                 return
             
             self.viz_text.insert(tk.END, result.stdout)
             
             # 2. Run
             if os.path.exists(vm_exe):
                   # program.evm is written to CWD by Java Frontend
                   vm_res = subprocess.run([vm_exe, "program.evm"], capture_output=True, text=True)
                   self.output_text.delete("1.0", tk.END)
                   self.output_text.insert(tk.END, "--- VM RUNTIME EXECUTION ---\n\n")
                   self.output_text.insert(tk.END, vm_res.stdout)
                   if vm_res.stderr:
                        self.output_text.insert(tk.END, "\n\nCRITICAL RUNTIME ERROR:\n" + vm_res.stderr)
             else:
                   self.output_text.delete("1.0", tk.END)
                   self.output_text.insert("1.0", f"SYSTEM ERROR: VM executable not found at '{vm_exe}'.\nPlease build all components first.\n")
                   
        except Exception as e:
            self.viz_text.insert(tk.END, f"\nSYSTEM EXCEPTION: {e}")

if __name__ == "__main__":
    app = EpsilonViz()
    app.mainloop()
