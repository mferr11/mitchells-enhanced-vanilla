"""
Generates the bow's arrow overlays and the bow item model definition.

The bow is drawn as two layers while pulling:
  1. the bow itself:  assets/mitchells-enhanced-vanilla/textures/item/bow/pulling_<stage>.png  (hand-drawn, no arrow)
  2. the arrow:       generated here from one sprite per arrow type in art/bow_arrows/

Each arrow sprite is drawn once, at the draw-stage-0 position (copy art/bow_arrows/arrow.png to start).
Vanilla moves the arrow back by 1 pixel diagonally per stage, so stages 1 and 2 are just shifted copies.

To add an arrow type:
  1. draw art/bow_arrows/<name>.png
  2. add a line to ARROWS below
  3. run:  python scripts/generate_bow_arrows.py
Requires Pillow (pip install pillow).
"""

import json
import re
from pathlib import Path

from PIL import Image

# Item ID of the ammo -> sprite name in art/bow_arrows/.
# The first entry is also used for any ammo not listed (e.g. spectral and tipped arrows).
ARROWS = {
    "minecraft:arrow": "arrow",
    "mitchells-enhanced-vanilla:reinforced_arrow": "reinforced_arrow",
    "mitchells-enhanced-vanilla:frozen_arrow": "frozen_arrow",
}

MOD_ID = "mitchells-enhanced-vanilla"
STAGES = 3
STAGE_SHIFT = (1, 1)            # pixels the arrow moves per stage (x, y)
STAGE_THRESHOLDS = [0.65, 0.9]  # pull amount at which stages 1 and 2 start (vanilla values)

ROOT = Path(__file__).resolve().parent.parent
ART = ROOT / "art" / "bow_arrows"
ASSETS = ROOT / "src" / "main" / "resources" / "assets"
TEXTURES = ASSETS / MOD_ID / "textures" / "item" / "bow_arrow"
MODELS = ASSETS / MOD_ID / "models" / "item" / "bow_arrow"
BOW_ITEM = ASSETS / "minecraft" / "items" / "bow.json"
BOW_STATS = ROOT / "src" / "main" / "java" / "mitchellenhancedvanilla" / "modid" / "BowStats.java"


def read_draw_ticks() -> float:
    match = re.search(r"DRAW_TICKS\s*=\s*([0-9.]+)F?", BOW_STATS.read_text(encoding="utf-8"))
    if not match:
        raise SystemExit(f"Couldn't find DRAW_TICKS in {BOW_STATS}")
    return float(match.group(1))


def shifted(sprite: Image.Image, dx: int, dy: int) -> Image.Image:
    out = Image.new("RGBA", sprite.size, (0, 0, 0, 0))
    out.paste(sprite, (dx, dy))  # pixels pushed off the edge are dropped
    return out


def model_ref(path: str) -> dict:
    return {"type": "minecraft:model", "model": f"{MOD_ID}:item/{path}"}


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def generate_overlays() -> None:
    TEXTURES.mkdir(parents=True, exist_ok=True)
    for name in ARROWS.values():
        sprite = Image.open(ART / f"{name}.png").convert("RGBA")
        if sprite.size != (16, 16):
            raise SystemExit(f"{name}.png must be 16x16, got {sprite.size}")
        for stage in range(STAGES):
            dx, dy = STAGE_SHIFT[0] * stage, STAGE_SHIFT[1] * stage
            shifted(sprite, dx, dy).save(TEXTURES / f"{name}_{stage}.png")
            write_json(MODELS / f"{name}_{stage}.json", {
                # Parent gives the overlay the same in-hand transforms as the bow
                "parent": "minecraft:item/bow",
                "textures": {"layer0": f"{MOD_ID}:item/bow_arrow/{name}_{stage}"},
            })


def stage_model(stage: int) -> dict:
    names = list(ARROWS.items())
    default_name = names[0][1]
    return {
        "type": "minecraft:composite",
        "models": [
            model_ref(f"bow/pulling_{stage}"),
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


def generate_bow_item(draw_ticks: float) -> None:
    write_json(BOW_ITEM, {
        "model": {
            "type": "minecraft:condition",
            "property": "minecraft:using_item",
            "on_false": {"type": "minecraft:model", "model": "minecraft:item/bow"},
            "on_true": {
                "type": "minecraft:range_dispatch",
                "property": "minecraft:use_duration",
                "scale": round(1 / draw_ticks, 6),
                "entries": [
                    {"threshold": threshold, "model": stage_model(stage + 1)}
                    for stage, threshold in enumerate(STAGE_THRESHOLDS)
                ],
                "fallback": stage_model(0),
            },
        }
    })


def main() -> None:
    draw_ticks = read_draw_ticks()
    generate_overlays()
    generate_bow_item(draw_ticks)
    print(f"Generated {len(ARROWS)} arrow overlay sets and bow.json (draw time {draw_ticks:g} ticks).")


if __name__ == "__main__":
    main()
