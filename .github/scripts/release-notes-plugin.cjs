const { execFileSync } = require("node:child_process");
const { readFileSync } = require("node:fs");

function loadJsonFromGit(ref, path) {
  try {
    const output = execFileSync("git", ["show", `${ref}:${path}`], {
      encoding: "utf8",
      stdio: ["ignore", "pipe", "ignore"],
    });
    return JSON.parse(output);
  } catch {
    return null;
  }
}

function loadJsonFile(path) {
  try {
    return JSON.parse(readFileSync(path, "utf8"));
  } catch {
    return null;
  }
}

function packageMap(patchesList) {
  const packages = new Map();

  for (const patch of patchesList?.patches || []) {
    for (const compatiblePackage of patch.compatiblePackages || []) {
      const packageName = compatiblePackage.packageName;
      if (!packageName) continue;

      if (!packages.has(packageName)) {
        packages.set(packageName, {
          name: compatiblePackage.name || packageName,
          versions: new Set(),
          patches: new Set(),
        });
      }

      const entry = packages.get(packageName);
      entry.patches.add(patch.name);

      for (const target of compatiblePackage.targets || []) {
        if (target.version) entry.versions.add(target.version);
      }
    }
  }

  return packages;
}

function sortedVersions(entry) {
  return [...entry.versions].sort();
}

function listItems(items, formatter, limit = 12) {
  const visible = items.slice(0, limit).map(formatter);
  const remaining = items.length - visible.length;
  if (remaining > 0) visible.push(`- ...and ${remaining} more`);
  return visible.join("\n");
}

function changedAppsSection(lastRelease, currentList) {
  if (!lastRelease?.gitTag) {
    return "## Supported Apps Changed\n\nNo previous release tag was found for comparison.";
  }

  const previousList = loadJsonFromGit(lastRelease.gitTag, "patches-list.json");
  if (!previousList || !currentList) {
    return "## Supported Apps Changed\n\nCould not compare supported apps for this release.";
  }

  const previousPackages = packageMap(previousList);
  const currentPackages = packageMap(currentList);

  const added = [...currentPackages.entries()]
    .filter(([packageName]) => !previousPackages.has(packageName))
    .sort((a, b) => a[1].name.localeCompare(b[1].name));

  const removed = [...previousPackages.entries()]
    .filter(([packageName]) => !currentPackages.has(packageName))
    .sort((a, b) => a[1].name.localeCompare(b[1].name));

  const updated = [...currentPackages.entries()]
    .filter(([packageName, current]) => {
      const previous = previousPackages.get(packageName);
      if (!previous) return false;
      return sortedVersions(previous).join("|") !== sortedVersions(current).join("|");
    })
    .sort((a, b) => a[1].name.localeCompare(b[1].name));

  const sections = ["## Supported Apps Changed"];

  if (added.length > 0) {
    sections.push(
      "",
      "### Added Apps",
      listItems(added, ([packageName, entry]) => `- **${entry.name}** (\`${packageName}\`)`)
    );
  }

  if (updated.length > 0) {
    sections.push("", "### Updated App Versions");
    sections.push(listItems(updated, ([packageName, current]) => {
      const previous = previousPackages.get(packageName);
      const before = sortedVersions(previous).join(", ") || "unknown";
      const after = sortedVersions(current).join(", ") || "unknown";
      return `- **${current.name}** (\`${packageName}\`): \`${before}\` -> \`${after}\``;
    }));
  }

  if (removed.length > 0) {
    sections.push(
      "",
      "### Removed Apps",
      listItems(removed, ([packageName, entry]) => `- **${entry.name}** (\`${packageName}\`)`)
    );
  }

  if (sections.length === 1) {
    sections.push("", "No supported app changes detected.");
  }

  return sections.join("\n");
}

function releaseHeader(context, currentList) {
  const version = context.nextRelease.version;
  const branchName = context.branch.name;
  const isPrerelease = Boolean(context.branch.prerelease) || version.includes("-");
  const channel = isPrerelease ? "dev prerelease" : "stable";
  const mppName = `patches-${version}.mpp`;
  const patchCount = currentList?.patches?.length;
  const patchCountLine = patchCount ? `**Patch count:** ${patchCount}` : null;

  return [
    `# Rushi's Patches v${version}`,
    "",
    "Patch source for Morphe.",
    "",
    `**Channel:** ${channel}  `,
    `**Branch:** \`${branchName}\`  `,
    patchCountLine,
    `**Install in Morphe:** https://morphe.software/add-source?github=rushiranpise/morphe-patches  `,
    `**Download:** \`${mppName}\` from the release assets below.`,
    "",
    isPrerelease
      ? "> This is a dev prerelease and may be less stable than a stable release."
      : null,
  ]
    .filter(Boolean)
    .join("\n");
}

function supportSection() {
  return [
    "## Need Help?",
    "",
    "If a patch fails, open a bug report and include:",
    "",
    "- App version",
    "- Patch source release/build",
    "- APK source/type",
    "- Logs",
    "",
    "Use the bug templates so the required details are included:",
    "https://github.com/rushiranpise/morphe-patches/issues/new/choose",
  ].join("\n");
}

function checksumSection() {
  return [
    "## Verification",
    "",
    "Download `SHA256SUMS.txt` from the release assets and compare it with the `.mpp` file you downloaded.",
  ].join("\n");
}

async function generateNotes(pluginConfig, context) {
  const releaseNotesGenerator = await import("@semantic-release/release-notes-generator");
  const generateConventionalNotes =
    releaseNotesGenerator.generateNotes ||
    releaseNotesGenerator.default?.generateNotes ||
    releaseNotesGenerator.default;

  const conventionalNotes = await generateConventionalNotes(
    pluginConfig.releaseNotesGenerator || {},
    context
  );
  const currentList = loadJsonFile("patches-list.json");

  return [
    releaseHeader(context, currentList),
    changedAppsSection(context.lastRelease, currentList),
    supportSection(),
    checksumSection(),
    conventionalNotes.trim(),
  ]
    .filter(Boolean)
    .join("\n\n");
}

module.exports = { generateNotes };
