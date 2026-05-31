from pathlib import Path
import subprocess
import sys
import textwrap

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
IMAGE_DIR = ROOT / "src" / "images"
WIDTH = 900
HEIGHT = 600


THEMES = {
    "NEW": ("#fff4cc", "#c61d28", "#f4c125"),
    "SUPER MEAL": ("#fff0dc", "#9b1a1f", "#f4a62a"),
    "HAPPY MEAL": ("#fff7d6", "#be1c24", "#79b84a"),
    "MEAL": ("#fff6e8", "#b51f2a", "#e9b949"),
    "BURGER": ("#fff2d5", "#8b3f1f", "#f4c125"),
    "FRIES": ("#fff1c7", "#c61d28", "#f4c125"),
    "DESSERT": ("#fff0f4", "#bd1e59", "#f8b7d4"),
    "DRINKS": ("#eef8ff", "#176b87", "#78c7e6"),
}


def font(size, bold=False):
    candidates = [
        "C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/calibrib.ttf" if bold else "C:/Windows/Fonts/calibri.ttf",
    ]

    for candidate in candidates:
        if Path(candidate).exists():
            return ImageFont.truetype(candidate, size=size)

    return ImageFont.load_default()


TITLE_FONT = font(42, True)
PRICE_FONT = font(34, True)
SMALL_FONT = font(24, True)


def fetch_foods():
    sql = "SELECT id, name, category, price FROM foods ORDER BY id"
    result = subprocess.run(
        [
            "mysql",
            "-uroot",
            "--batch",
            "--raw",
            "--skip-column-names",
            "macdolibee2",
            "-e",
            sql,
        ],
        cwd=ROOT,
        check=True,
        capture_output=True,
        text=True,
    )

    foods = []
    for line in result.stdout.splitlines():
        parts = line.split("\t")
        if len(parts) != 4:
            continue
        foods.append(
            {
                "id": int(parts[0]),
                "name": parts[1],
                "category": parts[2].upper(),
                "price": parts[3],
            }
        )

    return foods


def rounded(draw, box, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def draw_plate(draw, cx, cy, scale, accent):
    rounded(draw, [cx - 250 * scale, cy - 82 * scale, cx + 250 * scale, cy + 82 * scale], 70, "#ffffff", "#d9d0be", 5)
    draw.ellipse([cx - 205 * scale, cy - 62 * scale, cx + 205 * scale, cy + 62 * scale], fill="#f7f0df", outline="#d6c7a8", width=4)
    draw.ellipse([cx - 145 * scale, cy - 45 * scale, cx - 45 * scale, cy + 45 * scale], fill="#ffffff", outline="#e0d8c8", width=3)
    draw.polygon(
        [
            (cx + 5 * scale, cy - 36 * scale),
            (cx + 165 * scale, cy - 20 * scale),
            (cx + 115 * scale, cy + 42 * scale),
            (cx - 15 * scale, cy + 28 * scale),
        ],
        fill=accent,
        outline="#783518",
    )
    draw.arc([cx + 20 * scale, cy - 25 * scale, cx + 135 * scale, cy + 55 * scale], 190, 350, fill="#f7e1a2", width=5)


def draw_burger(draw, cx, cy, scale, accent):
    draw.ellipse([cx - 170 * scale, cy - 95 * scale, cx + 170 * scale, cy + 25 * scale], fill="#f2b35b", outline="#8b4a21", width=5)
    for x in [-95, -45, 15, 75]:
        draw.ellipse([cx + x * scale, cy - 58 * scale, cx + (x + 16) * scale, cy - 45 * scale], fill="#fff5d0")
    rounded(draw, [cx - 185 * scale, cy - 5 * scale, cx + 185 * scale, cy + 30 * scale], 18, "#47a447")
    rounded(draw, [cx - 178 * scale, cy + 22 * scale, cx + 178 * scale, cy + 62 * scale], 16, "#5a2f1a")
    draw.polygon([(cx - 145 * scale, cy + 62 * scale), (cx, cy + 104 * scale), (cx + 145 * scale, cy + 62 * scale)], fill=accent)
    rounded(draw, [cx - 160 * scale, cy + 98 * scale, cx + 160 * scale, cy + 142 * scale], 22, "#e8a84f", "#8b4a21", 4)


def draw_fries(draw, cx, cy, scale, accent):
    for x, h in [(-95, 185), (-55, 225), (-15, 195), (25, 230), (65, 205), (105, 180)]:
        rounded(draw, [cx + x * scale, cy - h * scale, cx + (x + 28) * scale, cy + 30 * scale], 10, "#ffd45d", "#b77c16", 3)
    draw.polygon(
        [(cx - 155 * scale, cy - 20 * scale), (cx + 155 * scale, cy - 20 * scale), (cx + 110 * scale, cy + 165 * scale), (cx - 110 * scale, cy + 165 * scale)],
        fill=accent,
        outline="#8c141a",
    )
    draw.rectangle([cx - 95 * scale, cy + 30 * scale, cx + 95 * scale, cy + 58 * scale], fill="#f4c125")


def draw_drink(draw, cx, cy, scale, accent):
    draw.line([cx + 45 * scale, cy - 180 * scale, cx + 115 * scale, cy - 260 * scale], fill="#8c141a", width=int(10 * scale))
    draw.line([cx + 115 * scale, cy - 260 * scale, cx + 185 * scale, cy - 260 * scale], fill="#8c141a", width=int(10 * scale))
    draw.polygon(
        [(cx - 130 * scale, cy - 160 * scale), (cx + 130 * scale, cy - 160 * scale), (cx + 90 * scale, cy + 175 * scale), (cx - 90 * scale, cy + 175 * scale)],
        fill="#ffffff",
        outline="#176b87",
    )
    rounded(draw, [cx - 105 * scale, cy - 120 * scale, cx + 105 * scale, cy + 20 * scale], 28, accent)
    draw.ellipse([cx - 100 * scale, cy - 185 * scale, cx + 100 * scale, cy - 130 * scale], fill="#f7fbff", outline="#176b87", width=4)
    draw.arc([cx - 75 * scale, cy - 110 * scale, cx + 75 * scale, cy + 15 * scale], 20, 330, fill="#ffffff", width=5)


def draw_dessert(draw, cx, cy, scale, accent):
    draw.ellipse([cx - 150 * scale, cy + 45 * scale, cx + 150 * scale, cy + 170 * scale], fill="#ffffff", outline="#bd1e59", width=5)
    draw.pieslice([cx - 135 * scale, cy - 35 * scale, cx - 5 * scale, cy + 95 * scale], 180, 360, fill="#f9d5e5", outline="#bd1e59")
    draw.pieslice([cx - 35 * scale, cy - 90 * scale, cx + 105 * scale, cy + 80 * scale], 180, 360, fill=accent, outline="#bd1e59")
    draw.pieslice([cx - 90 * scale, cy - 140 * scale, cx + 45 * scale, cy + 45 * scale], 180, 360, fill="#fff6f8", outline="#bd1e59")
    draw.ellipse([cx + 10 * scale, cy - 135 * scale, cx + 55 * scale, cy - 90 * scale], fill="#c61d28")
    draw.line([cx + 32 * scale, cy - 118 * scale, cx + 65 * scale, cy - 165 * scale], fill="#4c8d35", width=int(5 * scale))


def draw_combo(draw, cx, cy, scale, accent):
    draw_plate(draw, cx - 90 * scale, cy + 35 * scale, scale * 0.55, accent)
    draw_burger(draw, cx + 125 * scale, cy + 15 * scale, scale * 0.48, accent)
    draw_drink(draw, cx + 5 * scale, cy - 35 * scale, scale * 0.42, accent)


def draw_subject(draw, category, accent):
    cx, cy = WIDTH // 2, 250
    if "FRIES" in category:
        draw_fries(draw, cx, cy + 20, 1.0, accent)
    elif "DRINK" in category:
        draw_drink(draw, cx, cy + 15, 1.0, accent)
    elif "DESSERT" in category:
        draw_dessert(draw, cx, cy + 20, 1.0, accent)
    elif "BURGER" in category:
        draw_burger(draw, cx, cy + 10, 1.0, accent)
    elif "MEAL" in category:
        draw_combo(draw, cx, cy + 20, 1.0, accent)
    else:
        draw_plate(draw, cx, cy + 35, 1.0, accent)


def draw_wrapped_center(draw, text, y, max_width, font_obj, fill):
    lines = []
    for raw_line in textwrap.wrap(text, width=30):
        current = raw_line
        while draw.textbbox((0, 0), current, font=font_obj)[2] > max_width and len(current) > 8:
            cut = max(8, len(current) - 4)
            current = current[:cut]
        lines.append(current)

    if len(lines) > 2:
        lines = lines[:2]
        lines[-1] = lines[-1].rstrip(".") + "..."

    line_height = 48
    start_y = y - (len(lines) - 1) * line_height // 2
    for index, line in enumerate(lines):
        bbox = draw.textbbox((0, 0), line, font=font_obj)
        x = (WIDTH - (bbox[2] - bbox[0])) / 2
        draw.text((x, start_y + index * line_height), line, font=font_obj, fill=fill)


def generate(food):
    category = food["category"]
    bg, ink, accent = THEMES.get(category, ("#fff4cc", "#c61d28", "#f4c125"))
    image = Image.new("RGB", (WIDTH, HEIGHT), bg)
    draw = ImageDraw.Draw(image)

    draw.rounded_rectangle([30, 30, WIDTH - 30, HEIGHT - 30], radius=42, fill=bg, outline=ink, width=10)
    draw.rounded_rectangle([60, 60, WIDTH - 60, 460], radius=34, fill="#ffffff", outline=accent, width=8)
    draw_subject(draw, category, accent)
    draw.rounded_rectangle([85, 462, WIDTH - 85, 548], radius=28, fill=ink)
    draw_wrapped_center(draw, food["name"], 493, WIDTH - 210, TITLE_FONT, "#ffffff")
    draw.rounded_rectangle([WIDTH - 220, 72, WIDTH - 72, 128], radius=22, fill=accent)
    draw.text((WIDTH - 195, 82), f"PHP {food['price']}", font=PRICE_FONT, fill="#111111")
    draw.text((82, 84), category, font=SMALL_FONT, fill=ink)

    output = IMAGE_DIR / f"{food['id']}.png"
    image.save(output, "PNG", optimize=True)
    return output


def main():
    IMAGE_DIR.mkdir(parents=True, exist_ok=True)
    generated = []
    skipped = []

    for food in fetch_foods():
        output = IMAGE_DIR / f"{food['id']}.png"
        if output.exists():
            skipped.append(output.name)
            continue
        generated.append(generate(food).name)

    print(f"generated={len(generated)}")
    print(f"skipped_existing={len(skipped)}")
    if generated:
        print("generated_files=" + ", ".join(generated))


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(exc, file=sys.stderr)
        sys.exit(1)
