#!/usr/bin/env python3
"""
Simple script to generate placeholder launcher icons for the HIIT Timer app.
"""

try:
    from PIL import Image, ImageDraw, ImageFont
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False

import os

def create_icon(size, output_path):
    """Create a simple placeholder icon with the given size."""
    if not PIL_AVAILABLE:
        print(f"PIL not available, skipping {output_path}")
        return
    
    # Create image with blue background
    img = Image.new('RGB', (size, size), color='#1976D2')
    draw = ImageDraw.Draw(img)
    
    # Try to use a font, fall back to default if not available
    try:
        font_size = max(size // 6, 12)
        font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", font_size)
    except:
        try:
            font = ImageFont.load_default()
        except:
            font = None
    
    # Draw "HT" text in white
    text = "HT"
    if font:
        bbox = draw.textbbox((0, 0), text, font=font)
        text_width = bbox[2] - bbox[0]
        text_height = bbox[3] - bbox[1]
    else:
        text_width = size // 3
        text_height = size // 4
    
    x = (size - text_width) // 2
    y = (size - text_height) // 2
    
    draw.text((x, y), text, fill='white', font=font)
    
    # Save the image
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, 'PNG')
    print(f"Created {output_path}")

def main():
    """Generate all required launcher icons."""
    global PIL_AVAILABLE

    icons = [
        (72, 'app/src/main/res/mipmap-hdpi/ic_launcher.png'),
        (72, 'app/src/main/res/mipmap-hdpi/ic_launcher_round.png'),
        (48, 'app/src/main/res/mipmap-mdpi/ic_launcher.png'),
        (48, 'app/src/main/res/mipmap-mdpi/ic_launcher_round.png'),
        (96, 'app/src/main/res/mipmap-xhdpi/ic_launcher.png'),
        (96, 'app/src/main/res/mipmap-xhdpi/ic_launcher_round.png'),
        (144, 'app/src/main/res/mipmap-xxhdpi/ic_launcher.png'),
        (144, 'app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png'),
        (192, 'app/src/main/res/mipmap-xxxhdpi/ic_launcher.png'),
        (192, 'app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png'),
    ]

    if not PIL_AVAILABLE:
        print("PIL (Pillow) is not available. Installing...")
        os.system("pip3 install Pillow")
        try:
            from PIL import Image, ImageDraw, ImageFont
            PIL_AVAILABLE = True
        except ImportError:
            print("Could not install PIL. Creating empty placeholder files.")
            for size, path in icons:
                os.makedirs(os.path.dirname(path), exist_ok=True)
                with open(path, 'w') as f:
                    f.write("# Placeholder icon file\n")
            return

    for size, path in icons:
        create_icon(size, path)

if __name__ == '__main__':
    main()
