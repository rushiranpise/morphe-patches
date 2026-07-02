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
  if (!items.length) return "- None";

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

  const addedItems = listItems(
    added,
    ([packageName, entry]) => `- ${entry.name} (${packageName})`
  );
  const removedItems = listItems(
    removed,
    ([packageName, entry]) => `- ${entry.name} (${packageName})`
  );
  const updatedItems = listItems(updated, ([packageName, current]) => {
    const previous = previousPackages.get(packageName);
    const before = sortedVersions(previous).join(", ") || "unknown";
    const after = sortedVersions(current).join(", ") || "unknown";
    return `- ${current.name} (${packageName}): ${before} -> ${after}`;
  });

  return [
    "## Supported Apps Changed",
    "",
    "### Added Apps",
    addedItems,
    "",
    "### Updated App Versions",
    updatedItems,
    "",
    "### Removed Apps",
    removedItems,
  ].join("\n");
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
    `**Channel:** ${channel}`,
    `**Branch:** \`${branchName}\``,
    patchCountLine,
    `**Install source:** https://morphe.software/add-source?github=rushiranpise/morphe-patches`,
    `**Bundle asset:** download \`${mppName}\` from the assets below.`,
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
    "## Reporting Issues",
    "",
    "If a patch fails, include:",
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
    "A `SHA256SUMS.txt` asset is attached for verifying the downloaded `.mpp` bundle.",
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
    conventionalNotes.trim() ? `## Changes\n\n${conventionalNotes.trim()}` : "",
  ]
    .filter(Boolean)
    .join("\n\n");
}

module.exports = { generateNotes };
