#!/usr/bin/env python3
"""
Constants.kt maintenance script.

Three operations run in sequence on every invocation:

  1. REMOVE UNUSED — delete any *_COMPATIBILITY val that is never referenced
     in any *.kt file under patches/src/main/kotlin/app/template/patches/
     (excluding Constants.kt itself).

  2. DEDUPLICATE VERSIONS — for entries that carry more than one AppTarget,
     keep only the one with the highest versionCode.
     AppTarget(version = null, ...) entries are left untouched (pending APKs).
     AppTarget entries without a versionCode are also left untouched.

  3. SORT — sort all remaining *_COMPATIBILITY vals alphabetically by name
     (case-insensitive). Non-compatibility vals (e.g. LEAP_FITNESS_ALL) are
     preserved verbatim at the end of the file.

The file header (package declaration, imports, object/class opening brace) and
the final closing brace are preserved exactly.
"""

from __future__ import annotations
from pathlib import Path
import re
import sys

CONSTANTS_FILE = Path(
    "patches/src/main/kotlin/app/template/patches/shared/Constants.kt"
)

PATCHES_ROOT = Path(
    "patches/src/main/kotlin/app/template/patches"
)

# ─────────────────────────────────────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────────────────────────────────────

def find_matching_paren(text: str, open_pos: int) -> int:
    """
    Given the index of an opening '(' in *text*, return the index of its
    matching closing ')'.  Handles nested parentheses.
    """
    depth = 0
    for i in range(open_pos, len(text)):
        if text[i] == '(':
            depth += 1
        elif text[i] == ')':
            depth -= 1
            if depth == 0:
                return i
    return -1  # unbalanced — should never happen in valid Kotlin


def collect_used_names(patches_root: Path, constants_file: Path) -> set[str]:
    """
    Return the set of *_COMPATIBILITY names actually referenced in patch files.
    Constants.kt itself is excluded so self-declarations don't count as usage.

    Also scans any val = arrayOf(...) / listOf(...) blocks inside Constants.kt
    (e.g. LEAP_FITNESS_ALL) using balanced-paren matching so comment lines
    inside the array don't cause early termination.
    """
    used: set[str] = set()
    pattern = re.compile(r'[A-Z][A-Z0-9_]+_COMPATIBILITY')

    for kt_file in patches_root.rglob("*.kt"):
        if kt_file.resolve() == constants_file.resolve():
            continue
        for m in pattern.finditer(kt_file.read_text(encoding="utf-8")):
            used.add(m.group(0))

    # Scan val = arrayOf(...) / listOf(...) inside Constants.kt itself.
    # Uses find_matching_paren so that comment lines embedded in the array
    # (like the LEAP_FITNESS_ALL block) don't cause the scan to stop early.
    constants_text = constants_file.read_text(encoding="utf-8")
    for array_match in re.finditer(
        r'val\s+[A-Z0-9_]+\s*=\s*(?:arrayOf|listOf)\(',
        constants_text,
    ):
        open_pos = array_match.end() - 1   # position of the '('
        close_pos = find_matching_paren(constants_text, open_pos)
        if close_pos == -1:
            continue
        body = constants_text[open_pos + 1 : close_pos]
        for m in pattern.finditer(body):
            used.add(m.group(0))

    return used


def extract_blocks(content: str) -> list[tuple[str, str]]:
    """
    Split Constants.kt body into (name, block_text) pairs.

    Each block starts at 'val NAME' (preceding comment lines are stripped) and
    ends just before the next 'val NAME' or the final closing brace.
    """
    val_pattern = re.compile(r'(?m)^(?:[ \t]*//[^\n]*\n)*[ \t]*val\s+(\w+)\s*=')
    matches = list(val_pattern.finditer(content))

    blocks: list[tuple[str, str]] = []
    for idx, match in enumerate(matches):
        name = match.group(1)
        start = match.start() + match.group(0).rfind("val")
        end = (
            matches[idx + 1].start()
            if idx + 1 < len(matches)
            else content.rfind("}")
        )
        block = content[start:end].strip()
        blocks.append((name, block))

    return blocks


def extract_app_targets(targets_body: str) -> list[str]:
    """
    Extract each AppTarget(...) from the body of a listOf() call.
    Uses balanced-paren matching so multi-line / nested entries are handled.
    """
    results = []
    search_from = 0
    while True:
        start = targets_body.find("AppTarget(", search_from)
        if start == -1:
            break
        open_paren = targets_body.index("(", start)
        close_paren = find_matching_paren(targets_body, open_paren)
        results.append(targets_body[start : close_paren + 1])
        search_from = close_paren + 1
    return results


def keep_latest_target(block: str) -> str:
    """
    If a Compatibility block contains more than one AppTarget that has a numeric
    versionCode, replace the whole targets = listOf(...) with only the latest one
    (highest versionCode) on a single line.

    Entries without a versionCode or with version = null are left untouched.
    """
    list_start = block.find("targets = listOf(")
    if list_start == -1:
        return block

    open_paren = block.index("(", list_start + len("targets = listOf"))
    close_paren = find_matching_paren(block, open_paren)
    if close_paren == -1:
        return block

    targets_body = block[open_paren + 1 : close_paren]
    target_entries = extract_app_targets(targets_body)

    if len(target_entries) <= 1:
        return block

    # Skip if any entry has version = null (pending APK — preserve all)
    if any(re.search(r'version\s*=\s*null', t) for t in target_entries):
        return block

    # Skip if any entry is missing a versionCode (can't rank them safely)
    def version_code(entry: str) -> int | None:
        m = re.search(r'versionCode\s*=\s*(\d+)', entry)
        return int(m.group(1)) if m else None

    codes = [version_code(t) for t in target_entries]
    if any(c is None for c in codes):
        return block

    best_idx = max(range(len(codes)), key=lambda i: codes[i])  # type: ignore[arg-type]
    best = target_entries[best_idx]

    new_section = f"targets = listOf({best})"
    return block[: list_start] + new_section + block[close_paren + 1 :]


# ─────────────────────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────────────────────

def process(constants_file: Path, patches_root: Path) -> None:
    content = constants_file.read_text(encoding="utf-8")

    first_val = re.search(r'(?m)^[ \t]*val\s+', content)
    if not first_val:
        print("No val declarations found — nothing to do.")
        return

    header = content[: first_val.start()].rstrip()
    body   = content[first_val.start() :]

    # ── 1. collect names used in patch files ──────────────────────────────────
    used = collect_used_names(patches_root, constants_file)

    # ── 2. extract all blocks ─────────────────────────────────────────────────
    blocks = extract_blocks(body)

    compat_blocks: list[tuple[str, str]] = []
    other_blocks:  list[tuple[str, str]] = []

    for name, block in blocks:
        if name.endswith("_COMPATIBILITY"):
            compat_blocks.append((name, block))
        else:
            other_blocks.append((name, block))

    # ── 3. remove unused ─────────────────────────────────────────────────────
    before = len(compat_blocks)
    compat_blocks = [(n, b) for n, b in compat_blocks if n in used]
    removed = before - len(compat_blocks)
    if removed:
        print(f"Removed {removed} unused compatibility entr{'y' if removed == 1 else 'ies'}.")

    # ── 4. keep only latest AppTarget per block ───────────────────────────────
    deduped: list[tuple[str, str]] = []
    trimmed = 0
    for name, block in compat_blocks:
        new_block = keep_latest_target(block)
        if new_block != block:
            trimmed += 1
        deduped.append((name, new_block))
    if trimmed:
        print(f"Trimmed old AppTarget versions from {trimmed} entr{'y' if trimmed == 1 else 'ies'}.")

    # ── 5. sort alphabetically ────────────────────────────────────────────────
    deduped.sort(key=lambda x: x[0].lower())

    # ── 6. reassemble ────────────────────────────────────────────────────────
    all_blocks = deduped + other_blocks
    body_text  = "\n\n".join(b for _, b in all_blocks)
    result     = header + "\n\n" + body_text + "\n}\n"

    constants_file.write_text(result, encoding="utf-8")
    print(f"Done — {len(deduped)} compatibility entries retained.")


if __name__ == "__main__":
    if not CONSTANTS_FILE.exists():
        print(f"File not found: {CONSTANTS_FILE}", file=sys.stderr)
        sys.exit(1)

    process(CONSTANTS_FILE, PATCHES_ROOT)
    print(CONSTANTS_FILE)
