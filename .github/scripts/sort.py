#!/usr/bin/env python3

from pathlib import Path
import re


INPUT_FILE = Path(
    "patches/src/main/kotlin/app/template/patches/shared/Constants.kt"
)

OUTPUT_FILE = Path(
    "patches/src/main/kotlin/app/template/patches/shared/Constants.kt"
)


def extract_blocks(content):
    """
    Extract Kotlin compatibility blocks while preserving their content.
    Removes preceding comments.
    """

    pattern = re.compile(
        r'(?m)^\s*(?://.*\n\s*)*val\s+([A-Z0-9_]+_COMPATIBILITY)\s*='
    )

    matches = list(pattern.finditer(content))

    blocks = []

    for index, match in enumerate(matches):
        name = match.group(1)

        # Start exactly at val, skipping comments
        start = match.start() + match.group(0).rfind("val")

        # End at next block or before closing }
        if index + 1 < len(matches):
            end = matches[index + 1].start()
        else:
            end = content.rfind("}")

        block = content[start:end].strip()

        blocks.append((name, block))

    return blocks


def sort_kotlin_file(input_path, output_path):
    content = input_path.read_text(encoding="utf-8")

    blocks = extract_blocks(content)

    # Sort alphabetically
    blocks.sort(key=lambda x: x[0].lower())

    # Keep object declaration
    header = content[:content.find("val")].rstrip()

    result = (
        header
        + "\n\n"
        + "\n\n".join(block for _, block in blocks)
        + "\n}\n"
    )

    output_path.write_text(
        result,
        encoding="utf-8"
    )


if __name__ == "__main__":
    if not INPUT_FILE.exists():
        print(f"File not found: {INPUT_FILE}")
        exit(1)

    sort_kotlin_file(INPUT_FILE, OUTPUT_FILE)

    print(f"Sorted file created:")
    print(OUTPUT_FILE)