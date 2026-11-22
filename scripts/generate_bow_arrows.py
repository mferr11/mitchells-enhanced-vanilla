# Generates bow arrow overlays, pull-stage models and each bow's item definition from art/bow_arrows/<name>.png (stage-0 position).
# To add an arrow: draw the sprite, add it to ARROWS, run: python scripts/generate_bow_arrows.py
# To add a bow: add its textures, a BowStats constant and an entry in BOWS

import json
import re
from pathlib import Path

from PIL import Image

# Ammo item ID -> sprite name. First entry is the fallback for unlisted ammo.
ARROWS = {
    "minecraft:arrow": "arrow",
    "mitchells-enhanced-vanilla:reinforced_arrow": "reinforced_arrow",
    "mitchells-enhanced-vanilla:frozen_arrow": "frozen_arrow",
    "mitchells-enhanced-vanilla:copper_arrow": "copper_arrow",
    "mitchells-enhanced-vanilla:amethyst_arrow": "amethyst_arrow",
    "mitchells-enhanced-vanilla:gold_arrow": "gold_arrow",
    "mitchells-enhanced-vanilla:prismarine_arrow": "prismarine_arrow",
    "mitchells-enhanced-vanilla:emerald_arrow": "emerald_arrow",
}

MOD_ID = "mitchells-enhanced-vanilla"

# Bow item -> (BowStats constant, item definition path, idle model, pull-stage texture prefix under item/)
BOWS = {
    "minecraft:bow": ("WOODEN", "minecraft/items/bow.json", "minecraft:item/bow", "bow/pulling"),
    f"{MOD_ID}:iron_bow": ("IRON", f"{MOD_ID}/items/iron_bow.json", f"{MOD_ID}:item/iron_bow", "bow/iron_bow_pulling"),
    f"{MOD_ID}:golden_bow": ("GOLDEN", f"{MOD_ID}/items/golden_bow.json", f"{MOD_ID}:item/golden_bow", "bow/gold_bow_pulling"),
    f"{MOD_ID}:diamond_bow": ("DIAMOND", f"{MOD_ID}/items/diamond_bow.json", f"{MOD_ID}:item/diamond_bow", "bow/diamond_bow_pulling"),
    f"{MOD_ID}:netherite_bow": ("NETHERITE", f"{MOD_ID}/items/netherite_bow.json", f"{MOD_ID}:item/netherite_bow", "bow/netherite_bow_pulling"),
}
STAGES = 3
STAGE_SHIFT = (1, 1)            # pixels per stage (x, y)
STAGE_THRESHOLDS = [0.65, 0.9]  # pull amount for stages 1 and 2

ROOT = Path(__file__).resolve().parent.parent
ART = ROOT / "art" / "bow_arrows"
ASSETS = ROOT / "src" / "main" / "resources" / "assets"
TEXTURES = ASSETS / MOD_ID / "textures" / "item" / "bow_arrow"
ITEM_MODELS = ASSETS / MOD_ID / "models" / "item"
MODELS = ITEM_MODELS / "bow_arrow"
ITEM_TEXTURES = ASSETS / MOD_ID / "textures" / "item"

# Shaft colours drawn in the art sprites (light side, dark side), replaced with each arrow's own
ART_SHAFT = [(0x88, 0x65, 0x20, 255), (0x21, 0x15, 0x02, 255)]
BOW_STATS = ROOT / "src" / "main" / "java" / "mitchellenhancedvanilla" / "modid" / "BowStats.java"


def read_draw_ticks(constant: str) -> float:
    match = re.search(rf"\b{constant}\s*=\s*new BowStats\(\s*([0-9.]+)F?", BOW_STATS.read_text(encoding="utf-8"))
    if not match:
        raise SystemExit(f"Couldn't find BowStats.{constant} in {BOW_STATS}")
    return float(match.group(1))


def shifted(sprite: Image.Image, dx: int, dy: int) -> Image.Image:
    out = Image.new("RGBA", sprite.size, (0, 0, 0, 0))
    out.paste(sprite, (dx, dy))
    return out


def model_ref(path: str) -> dict:
    return {"type": "minecraft:model", "model": f"{MOD_ID}:item/{path}"}


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def shaft_pixels(image: Image.Image, colour: tuple) -> list:
    # Pixels of one shaft colour, ordered from the tip end (top) down
    return sorted(((x, y) for x in range(16) for y in range(16) if image.getpixel((x, y)) == colour),
                  key=lambda p: p[1])


def item_shaft(item: Image.Image, edge: int) -> list:
    # Shaft colours from the item texture, tip end first. edge 0 = light side (x+y=15), 1 = dark side (x+y=16)
    xs = range(11, 4 - edge, -1) if edge == 0 else range(11, 5, -1)
    colours = [item.getpixel((x, 15 + edge - x)) for x in xs]
    if edge == 1:
        colours.append(item.getpixel((5, 11)))
    if any(c[3] == 0 for c in colours):
        raise SystemExit("Item texture shaft isn't where the vanilla arrow's is")
    return colours


def recolour_shaft(sprite: Image.Image, name: str) -> Image.Image:
    # Copies the arrow's own shaft colours (from its item texture) onto the in-bow sprite
    item_path = ITEM_TEXTURES / f"{name}.png"
    if not item_path.exists():
        return sprite
    item = Image.open(item_path).convert("RGBA")
    out = sprite.copy()
    for edge, art_colour in enumerate(ART_SHAFT):
        targets = shaft_pixels(sprite, art_colour)
        colours = item_shaft(item, edge)
        for i, pos in enumerate(targets):
            # Stretch the item's shaft over the sprite's so gradients keep their order
            j = round(i * (len(colours) - 1) / max(len(targets) - 1, 1))
            out.putpixel(pos, colours[j])
    return out


def generate_overlays() -> None:
    TEXTURES.mkdir(parents=True, exist_ok=True)
    for name in ARROWS.values():
        sprite = Image.open(ART / f"{name}.png").convert("RGBA")
        if sprite.size != (16, 16):
            raise SystemExit(f"{name}.png must be 16x16, got {sprite.size}")
        sprite = recolour_shaft(sprite, name)
        for stage in range(STAGES):
            dx, dy = STAGE_SHIFT[0] * stage, STAGE_SHIFT[1] * stage
            shifted(sprite, dx, dy).save(TEXTURES / f"{name}_{stage}.png")
            write_json(MODELS / f"{name}_{stage}.json", {
                # Same in-hand transforms as the bow
                "parent": "minecraft:item/bow",
                "textures": {"layer0": f"{MOD_ID}:item/bow_arrow/{name}_{stage}"},
            })


def stage_model(pulling: str, stage: int) -> dict:
    names = list(ARROWS.items())
    default_name = names[0][1]
    return {
        "type": "minecraft:composite",
        "models": [
            model_ref(f"{pulling}_{stage}"),
            {
                "type": "minecraft:select",
                "property": f"{MOD_ID}:bow_ammo",
                "cases": [
                    {"when": item_id, "model": model_ref(f"bow_arrow/{name}_{stage}")}
                    for item_id, name in names[1:]
                ],
                "fallback": model_ref(f"bow_arrow/{default_name}_{stage}"),
            },
        ],
    }


def generate_bow(constant: str, item_file: str, idle: str, pulling: str) -> float:
    draw_ticks = read_draw_ticks(constant)
    # Idle model for modded bows (vanilla's already exists)
    if idle.startswith(f"{MOD_ID}:"):
        name = idle.split("/")[-1]
        write_json(ITEM_MODELS / f"{name}.json", {"parent": "minecraft:item/bow", "textures": {"layer0": idle}})
    for stage in range(STAGES):
        write_json(ITEM_MODELS / f"{pulling}_{stage}.json", {
            "parent": "minecraft:item/bow",
            "textures": {"layer0": f"{MOD_ID}:item/{pulling}_{stage}"},
        })
    write_json(ASSETS / item_file, {
        "model": {
            "type": "minecraft:condition",
            "property": "minecraft:using_item",
            "on_false": {"type": "minecraft:model", "model": idle},
            "on_true": {
                "type": "minecraft:range_dispatch",
                "property": "minecraft:use_duration",
                "scale": round(1 / draw_ticks, 6),
                "entries": [
                    {"threshold": threshold, "model": stage_model(pulling, stage + 1)}
                    for stage, threshold in enumerate(STAGE_THRESHOLDS)
                ],
                "fallback": stage_model(pulling, 0),
            },
        }
    })
    return draw_ticks


def main() -> None:
    generate_overlays()
    for item_id, (constant, item_file, idle, pulling) in BOWS.items():
        draw_ticks = generate_bow(constant, item_file, idle, pulling)
        print(f"{item_id}: draw time {draw_ticks:g} ticks")
    print(f"Generated {len(ARROWS)} arrow overlay sets for {len(BOWS)} bows.")


if __name__ == "__main__":
    main()
