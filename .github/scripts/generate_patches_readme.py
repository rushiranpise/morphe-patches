#!/usr/bin/env python3
"""
Generates the patches section of README.md from patches-list.json
and injects it between <!-- PATCHES_START --> / <!-- PATCHES_END --> markers.

Details blocks are expanded (open by default) if:
  1. Total patch count <= AUTO_EXPAND_THRESHOLD.
  2. The README marker explicitly says: <!-- PATCHES_START EXPANDED -->

python3 generate_patches_readme.py <owner/repo> <branch> [patches-list.json] [README.md]
"""

import json
import re
import sys
from html import escape
from pathlib import Path


if len(sys.argv) < 3:
    print("Usage: generate_patches_readme.py <owner/repo> <branch> [json] [README]")
    sys.exit(1)

repo_full = sys.argv[1]
branch = sys.argv[2]
json_path = Path(sys.argv[3]) if len(sys.argv) > 3 else Path("patches-list.json")
readme_path = Path(sys.argv[4]) if len(sys.argv) > 4 else Path("README.md")

if "/" not in repo_full:
    raise ValueError(f"Invalid repo format: {repo_full} (expected owner/repo)")

owner, repo = repo_full.split("/", 1)

with open(json_path, encoding="utf-8") as f:
    data = json.load(f)


# Group patches by package; patches with no compatiblePackages are universal.
# JSON structure: compatiblePackages is a list of objects with
# { packageName, name, targets: [{ version, isExperimental, description }] }
by_pkg = {}  # packageName -> { name, patches, targets }
universal = {}

for patch in data["patches"]:
    compatible_packages = patch.get("compatiblePackages")
    if not compatible_packages:
        # Deduplicate universal patches by name.
        if patch["name"] not in universal:
            universal[patch["name"]] = patch
        continue

    for pkg_entry in compatible_packages:
        package_name = pkg_entry["packageName"]
        app_name = pkg_entry.get("name") or package_name
        if package_name not in by_pkg:
            by_pkg[package_name] = {
                "name": app_name,
                "patches": {},
                "targets": pkg_entry.get("targets", []),
            }

        # Deduplicate patches that appear across multiple packages.
        if patch["name"] not in by_pkg[package_name]["patches"]:
            by_pkg[package_name]["patches"][patch["name"]] = patch


def anchor(name):
    """Convert a patch name to a GitHub-compatible anchor slug."""
    return re.sub(r"-+", "-", re.sub(r"[^a-z0-9]+", "-", name.lower())).strip("-")


def play_store_link(package_name):
    """Generate Google Play Store URL from package name."""
    return f"https://play.google.com/store/apps/details?id={package_name}"


def description_summary(text, max_length=180):
    """Keep README rows compact by showing the first useful description line."""
    first_line = ""
    for line in (text or "").splitlines():
        stripped = line.strip()
        if stripped:
            first_line = stripped
            break

    cleaned = re.sub(r"\s+", " ", first_line).strip()
    if not cleaned:
        return ""

    match = re.search(r"(?<=[.!?])\s+", cleaned)
    summary = cleaned[: match.start()].strip() if match else cleaned
    if len(summary) <= max_length:
        return summary

    return summary[: max_length - 1].rstrip() + "..."


def options_summary(options, visible=3):
    """Render a compact options summary for a table details cell."""
    titles = [opt.get("title") or opt.get("key") for opt in options or []]
    titles = [title for title in titles if title]
    if not titles:
        return ""

    shown = titles[:visible]
    remaining = len(titles) - len(shown)
    summary = ", ".join(escape(title, quote=False) for title in shown)
    if remaining > 0:
        summary += f", +{remaining} more"
    return f"<br><sub>Options: {summary}</sub>"


def patches_table(patches):
    """Render a sorted markdown table of patches with compact details."""
    rows = [
        "| Patch | Details |",
        "|---|---|",
    ]

    for patch in sorted(patches, key=lambda p: p["name"]):
        patch_anchor = anchor(patch["name"])
        description = escape(description_summary(patch.get("description") or ""), quote=False)
        details = description + options_summary(patch.get("options") or [])
        rows.append(f"| [**{escape(patch['name'])}**](#{patch_anchor}) | {details} |")

    return "\n".join(rows)


def versions_line(targets):
    """Render supported versions as compact inline code badges."""
    if not targets:
        return ""

    versions = []
    for target in targets:
        version = target["version"]
        if version is None:
            continue

        label = f"experimental {version}" if target.get("isExperimental") else version
        versions.append(label)

    if not versions:
        return ""

    return "**Supported versions:** " + " ".join(f"`{escape(version)}`" for version in versions)


def details_block(label, count, targets, table, expanded=False, package_name=None, index=None):
    """Wrap a patches table in a details block with compact metadata."""
    noun = "patch" if count == 1 else "patches"
    versions = versions_line(targets)
    versions_section = f"{versions}\n\n" if versions else ""
    tag = "<details open>" if expanded else "<details>"

    prefix = f"<code>#{index}</code> " if index is not None else ""
    summary = f"{prefix}<strong>{escape(label, quote=False)}</strong> &middot; {count} {noun}"
    if package_name:
        summary += (
            f" &middot; <code>{escape(package_name, quote=False)}</code>"
            f" &middot; <a href=\"{play_store_link(package_name)}\">Play Store</a>"
        )

    return f"""{tag}
<summary>{summary}</summary>
<br>

{versions_section}{table}

</details>"""


def build_content(expanded=False):
    """Build the full generated patches section."""
    lines = [
        f"> **[v{ver}](https://github.com/{owner}/{repo}/releases/tag/v{ver})**"
        f"&nbsp;&nbsp;&middot;&nbsp;&nbsp;`{branch}`&nbsp;&nbsp;&middot;&nbsp;&nbsp;"
        f"{total} patches total"
    ]

    # One details block per app, sorted by app name for predictable scanning.
    sorted_packages = sorted(by_pkg.items(), key=lambda item: item[1]["name"].lower())
    for index, (package_name, entry) in enumerate(sorted_packages, start=1):
        patches = list(entry["patches"].values())
        lines.append(
            details_block(
                entry["name"],
                len(patches),
                entry["targets"],
                patches_table(patches),
                expanded,
                package_name=package_name,
                index=index,
            )
        )
        lines.append("")

    # Universal patches with no specific app.
    if universal:
        universal_patches = list(universal.values())
        lines.append(
            details_block(
                "Universal",
                len(universal_patches),
                [],
                patches_table(universal_patches),
                expanded,
                index=len(sorted_packages) + 1,
            )
        )
        lines.append("")

    return "\n".join(lines)


# Build and inject.
raw_ver = data["version"]
ver = raw_ver.lstrip("v")
total = sum(len(entry["patches"]) for entry in by_pkg.values()) + len(universal)

readme = readme_path.read_text(encoding="utf-8")

# Marker pattern matches both <!-- PATCHES_START --> and <!-- PATCHES_START EXPANDED -->.
START_PATTERN = r"<!-- PATCHES_START(?:\s+EXPANDED)?\s*-->"
END_MARKER = "<!-- PATCHES_END -->"

marker_match = re.search(START_PATTERN, readme)

if not marker_match or END_MARKER not in readme:
    # Fallback: print to stdout so CI can catch the issue.
    print(build_content(expanded=False))
    sys.stderr.write(
        f"Markers <!-- PATCHES_START [EXPANDED] --> / {END_MARKER} not found in {readme_path}. "
        "Printed to stdout instead.\n"
    )
    sys.exit(1)

actual_start = marker_match.group(0)

AUTO_EXPAND_THRESHOLD = 20

# Details blocks are expanded if:
# 1. Total patch count is small (<= AUTO_EXPAND_THRESHOLD).
# 2. The README marker explicitly requests it: <!-- PATCHES_START EXPANDED -->.
expanded = total <= AUTO_EXPAND_THRESHOLD or "EXPANDED" in actual_start

generated = build_content(expanded=expanded)
new_readme = re.sub(
    rf"{START_PATTERN}.*?{re.escape(END_MARKER)}",
    f"{actual_start}\n{generated}\n{END_MARKER}",
    readme,
    flags=re.DOTALL,
)
readme_path.write_text(new_readme, encoding="utf-8")
print(f"Injected patches section into {readme_path} (v{ver}, branch={branch}, {total} patches, expanded={expanded})")
