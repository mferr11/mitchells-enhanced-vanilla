# Builds the Barrow King's Skull texture: his head on top, the Tarnished Crown's helmet layer underneath.
# The skull model reads the head from rows 0-31 and the crown (an inflated outer cube) from rows 32-63.
# Re-run after editing either source texture: python scripts/generate_barrow_king_skull.py

from pathlib import Path

from PIL import Image

TEXTURES = Path(__file__).resolve().parent.parent / "src/main/resources/assets/mitchells-enhanced-vanilla/textures/entity"
HEAD = TEXTURES / "barrow_king/barrow_king.png"
CROWN = TEXTURES / "equipment/humanoid/tarnished_crown.png"
OUTPUT = TEXTURES / "barrow_king/barrow_king_skull.png"


def main():
    head = Image.open(HEAD).convert("RGBA")
    crown = Image.open(CROWN).convert("RGBA")
    if head.size != (64, 32) or crown.size != (64, 32):
        raise SystemExit(f"Expected 64x32 textures, got head {head.size} and crown {crown.size}")

    skull = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    skull.paste(head, (0, 0))
    skull.paste(crown, (0, 32))
    skull.save(OUTPUT)
    print(f"Wrote {OUTPUT}")


if __name__ == "__main__":
    main()
