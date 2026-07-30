import tkinter as tk
from tkinter import ttk
from PIL import Image, ImageTk
import qrcode

def generate_qr():
    data = entry.get().strip()

    if not data:
        return

    # Generate QR
    qr = qrcode.make(data)

    # Resize for display
    qr = qr.resize((250, 250))

    # Convert to Tkinter image
    img = ImageTk.PhotoImage(qr)

    # Update label
    qr_label.config(image=img)
    qr_label.image = img  # keep reference


# Create window
root = tk.Tk()
root.title("TOTP QR Generator")
root.geometry("350x400")

# Input label
label = ttk.Label(root, text="Enter otpauth URL:")
label.pack(pady=10)

# Input field
entry = ttk.Entry(root, width=40)
entry.pack(pady=5)

# Generate button
btn = ttk.Button(root, text="Generate QR", command=generate_qr)
btn.pack(pady=10)

# QR display area
qr_label = ttk.Label(root)
qr_label.pack(pady=20)

# Run app
root.mainloop()