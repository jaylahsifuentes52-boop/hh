import tkinter as tk
from tkinter import filedialog, messagebox
import os
import re

APP_NAME = "Junior"

# Defensive scanner only: it reports suspicious anti-cheat-related strings
# and does not modify or remove anything from the selected file.
PATTERNS = [
    rb"anti.?cheat",
    rb"easyanticheat",
    rb"easy.?anti.?cheat",
    rb"battleye",
    rb"be.?anti.?cheat",
    rb"vgk",
    rb"vanguard",
    rb"nprotect",
    rb"gameguard",
    rb"integrity.?check",
    rb"tamper",
    rb"cheat.?detection",
]

def browse():
    path = filedialog.askopenfilename(
        title="Browse data.unity3d",
        filetypes=[("Unity data files", "*.unity3d"), ("All files", "*.*")]
    )
    if path:
        file_var.set(path)
        status_var.set("File selected. Click Scan for Anti-Cheat.")

def scan():
    path = file_var.get().strip()
    if not path or not os.path.isfile(path):
        messagebox.showerror(APP_NAME, "Please select a data.unity3d file first.")
        return

    status_var.set("Scanning for anti-cheat indicators...")
    root.update_idletasks()

    try:
        with open(path, "rb") as f:
            data = f.read()

        found = []
        for pattern in PATTERNS:
            if re.search(pattern, data, re.IGNORECASE):
                found.append(pattern.decode("ascii", errors="ignore"))

        if found:
            result = (
                "Scan complete.\n\n"
                "Potential anti-cheat indicators were found.\n"
                "No data was removed or modified.\n\n"
                "Indicators:\n- " + "\n- ".join(found)
            )
            status_var.set("Scan complete — potential indicators found.")
        else:
            result = (
                "Scan complete.\n\n"
                "No known anti-cheat indicators were found.\n"
                "No data was modified."
            )
            status_var.set("Scan complete — no known indicators found.")

        output.delete("1.0", tk.END)
        output.insert(tk.END, result)

    except Exception as e:
        status_var.set("Scan failed.")
        messagebox.showerror(APP_NAME, f"Could not scan the file:\n{e}")

def save_report():
    text = output.get("1.0", tk.END).strip()
    if not text:
        messagebox.showinfo(APP_NAME, "Run a scan first.")
        return

    downloads = os.path.join(os.path.expanduser("~"), "Downloads")
    os.makedirs(downloads, exist_ok=True)
    path = filedialog.asksaveasfilename(
        title="Save scan report",
        initialdir=downloads,
        initialfile="data-Junior-AntiCheat-Scan.txt",
        defaultextension=".txt",
        filetypes=[("Text report", "*.txt")]
    )
    if path:
        with open(path, "w", encoding="utf-8") as f:
            f.write(text)
        messagebox.showinfo(APP_NAME, f"Report saved:\n{path}")

root = tk.Tk()
root.title("Junior — Anti-Cheat Scanner")
root.geometry("700x500")

tk.Label(root, text="Junior", font=("Arial", 24, "bold")).pack(pady=(18, 2))
tk.Label(root, text="Anti-Cheat Scanner", font=("Arial", 14)).pack(pady=(0, 15))

file_var = tk.StringVar()
tk.Entry(root, textvariable=file_var, width=72).pack(padx=20, pady=5)

buttons = tk.Frame(root)
buttons.pack(pady=8)
tk.Button(buttons, text="Browse data.unity3d", command=browse, width=22).pack(side="left", padx=5)
tk.Button(buttons, text="Scan", command=scan, width=14).pack(side="left", padx=5)
tk.Button(buttons, text="Save Report", command=save_report, width=14).pack(side="left", padx=5)

status_var = tk.StringVar(value="Choose a data.unity3d file.")
tk.Label(root, textvariable=status_var).pack(pady=8)

output = tk.Text(root, wrap="word", height=16)
output.pack(fill="both", expand=True, padx=20, pady=(5, 20))

root.mainloop()
