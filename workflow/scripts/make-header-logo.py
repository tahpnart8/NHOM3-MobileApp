# Builds the two header logo resources from the official horizontal logo (issue #397).
#
#   ic_logo_header.webp             original colours, for light backgrounds (sign-in screen A01)
#   ic_logo_header_on_primary.webp  the word "PUBGApp" turned white, for the green Home top bar
#
# Only the text part changes colour: every pixel right of the transparent gap that separates the
# illustration from the word gets RGB white with its alpha kept, so the anti-aliased edges stay smooth.
# The illustration (hands, box, sparkles) is untouched. Both files are trimmed of fully transparent
# margins and scaled to 192 px high (48dp at xxxhdpi), aspect ratio kept.
#
# Run (needs Pillow):
#   python workflow/scripts/make-header-logo.py <pubgapp_header_horizontal_transparent.png> PUBGApp/app/src/main/res/drawable-nodpi

import os
import sys

from PIL import Image

OUTPUT_HEIGHT = 192
WEBP_QUALITY = 90
# A column counts as empty when no pixel in it is more than this opaque.
ALPHA_THRESHOLD = 16
# The gap between illustration and word is about 34 px in the original; letters are closer than this.
MIN_GAP_WIDTH = 20


def column_is_empty(pixels, x, height):
    for y in range(height):
        if pixels[x, y][3] > ALPHA_THRESHOLD:
            return False
    return True


def find_text_start(image):
    """Returns the first column of the word, after the first wide transparent gap."""
    pixels = image.load()
    width, height = image.size
    seen_content = False
    gap_start = -1
    for x in range(width):
        empty = column_is_empty(pixels, x, height)
        if not empty and not seen_content:
            seen_content = True
        elif empty and seen_content and gap_start < 0:
            gap_start = x
        elif not empty and gap_start >= 0:
            if x - gap_start >= MIN_GAP_WIDTH:
                return x
            gap_start = -1
    raise SystemExit("No gap between the illustration and the word was found.")


def whiten_from(image, start_x):
    result = image.copy()
    pixels = result.load()
    width, height = result.size
    for x in range(start_x, width):
        for y in range(height):
            alpha = pixels[x, y][3]
            if alpha > 0:
                pixels[x, y] = (255, 255, 255, alpha)
    return result


def trim_and_scale(image):
    left, top, right, bottom = image.getchannel("A").getbbox()
    pad = 4
    box = (max(0, left - pad), max(0, top - pad), min(image.width, right + pad), min(image.height, bottom + pad))
    cropped = image.crop(box)
    width = round(cropped.width * OUTPUT_HEIGHT / cropped.height)
    return cropped.resize((width, OUTPUT_HEIGHT), Image.LANCZOS)


def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: make-header-logo.py <original png> <drawable-nodpi folder>")
    original = Image.open(sys.argv[1]).convert("RGBA")
    out_dir = sys.argv[2]
    text_start = find_text_start(original)
    print("word starts at column", text_start)
    trim_and_scale(original).save(os.path.join(out_dir, "ic_logo_header.webp"), quality=WEBP_QUALITY)
    white = whiten_from(original, text_start)
    trim_and_scale(white).save(os.path.join(out_dir, "ic_logo_header_on_primary.webp"), quality=WEBP_QUALITY)


main()
