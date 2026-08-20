#!/usr/bin/env node

import { execFileSync } from "node:child_process";
import { existsSync } from "node:fs";
import { dirname, isAbsolute, relative, resolve, sep } from "node:path";
import { posix } from "node:path";

const STATUS_NAMES = {
  A: "added",
  C: "copied",
  D: "deleted",
  M: "modified",
  R: "renamed",
  T: "type-changed",
  U: "unmerged",
  X: "unknown",
  B: "broken-pairing",
  "?": "untracked",
  "!": "ignored",
  " ": "unmodified",
};

const REPO_GOVERNANCE_DIRS = new Set([
  ".codex",
  ".github",
  ".githooks",
  ".idea",
  ".vscode",
]);

const usage = `Usage:
  collect-review-context.mjs context [--base <ref>]
  collect-review-context.mjs snapshot [--base <ref>]
  collect-review-context.mjs diff [--base <ref>] [--module <name> ... | --path <path> ...]`;

const runGit = (repoRoot, args, options = {}) => {
  try {
    return execFileSync("git", args, {
      cwd: repoRoot,
      encoding: options.binary ? null : "utf8",
      input: options.input,
      maxBuffer: 1024 * 1024 * 1024,
      stdio: [options.input ? "pipe" : "ignore", "pipe", "pipe"],
    });
  } catch (error) {
    const detail = error.stderr?.toString("utf8").trim();
    throw new Error(detail || `git ${args.join(" ")} failed`);
  }
};

const findRepoRoot = () => {
  try {
    return resolve(
      execFileSync("git", ["rev-parse", "--show-toplevel"], {
        cwd: process.cwd(),
        encoding: "utf8",
        stdio: ["ignore", "pipe", "pipe"],
      }).trim(),
    );
  } catch {
    throw new Error("current directory is not inside a Git repository");
  }
};

const parseArgs = (args) => {
  if (args.length === 0 || args.includes("-h") || args.includes("--help")) {
    console.log(usage);
    process.exit(0);
  }

  const command = args[0];
  if (!new Set(["context", "snapshot", "diff"]).has(command)) {
    throw new Error(`unknown command: ${command}`);
  }

  const options = { command, base: "main", modules: [], paths: [] };
  for (let index = 1; index < args.length; index += 1) {
    const arg = args[index];
    const value = args[index + 1];
    if (!["--base", "--module", "--path"].includes(arg)) {
      throw new Error(`unexpected argument: ${arg}`);
    }
    if (!value || value.startsWith("-")) {
      throw new Error(`${arg} requires a value`);
    }
    index += 1;
    if (arg === "--base") {
      options.base = value;
    } else if (arg === "--module") {
      options.modules.push(value);
    } else {
      options.paths.push(value);
    }
  }

  if (
    command !== "diff" &&
    (options.modules.length > 0 || options.paths.length > 0)
  ) {
    throw new Error("--module and --path are only valid for the diff command");
  }
  if (options.modules.length > 0 && options.paths.length > 0) {
    throw new Error("--module and --path cannot be combined");
  }
  return options;
};

const normalizeBase = (repoRoot, base) => {
  if (!base.trim() || base.startsWith("-")) {
    throw new Error("base ref must be a non-empty Git ref");
  }
  return {
    baseRef: base,
    baseSha: runGit(repoRoot, [
      "rev-parse",
      "--verify",
      `${base}^{commit}`,
    ]).trim(),
    mergeBase: runGit(repoRoot, ["merge-base", base, "HEAD"]).trim(),
  };
};

const moduleForPath = (repoRoot, rawPath) => {
  const parts = rawPath.split("/").filter(Boolean);
  if (parts.length === 0) {
    return "other";
  }

  const top = parts[0];
  if (parts.length === 1 || REPO_GOVERNANCE_DIRS.has(top)) {
    return "repo-governance";
  }
  if (top === "docs") {
    return "docs";
  }
  if (top === "db") {
    return "db-seed";
  }
  if (top === "deploy") {
    return "deploy";
  }
  if (top === "kuzhambu-apps") {
    return parts.length > 2 ? `apps:${parts[1]}` : "apps:workspace";
  }
  if (top === "kuzhambu-workers") {
    return "workers";
  }
  if (
    (top === "scripts" && parts[1] === "seed") ||
    rawPath === "scripts/import-seed-data.sh"
  ) {
    return "db-seed";
  }
  if (top !== "kuzhambu-servers") {
    return "other";
  }

  const serversRoot = resolve(repoRoot, "kuzhambu-servers");
  let candidate = dirname(resolve(repoRoot, rawPath));
  while (
    candidate !== serversRoot &&
    candidate.startsWith(`${serversRoot}${sep}`)
  ) {
    if (existsSync(resolve(candidate, "pom.xml"))) {
      return `servers:${relative(serversRoot, candidate).split(sep).join("/")}`;
    }
    candidate = dirname(candidate);
  }
  return parts.length > 1 ? `servers:${parts[1]}` : "servers:unknown";
};

const parseChangedFiles = (repoRoot, baseRef) => {
  const fields = runGit(
    repoRoot,
    ["diff", "--name-status", "-z", "--find-renames", `${baseRef}...HEAD`],
    { binary: true },
  )
    .toString("utf8")
    .split("\0");
  if (fields.at(-1) === "") {
    fields.pop();
  }

  const changedFiles = [];
  for (let index = 0; index < fields.length;) {
    const statusToken = fields[index++];
    const statusCode = statusToken[0];
    let oldPath;
    let path;
    if (new Set(["R", "C"]).has(statusCode)) {
      oldPath = fields[index++];
      path = fields[index++];
    } else {
      path = fields[index++];
    }
    if (!path) {
      throw new Error("unexpected truncated --name-status output");
    }

    const entry = {
      status: STATUS_NAMES[statusCode] ?? "unknown",
      path,
      module: moduleForPath(repoRoot, path),
    };
    if (oldPath) {
      entry.old_path = oldPath;
    }
    if (statusToken.length > 1) {
      entry.similarity = statusToken.slice(1);
    }
    changedFiles.push(entry);
  }
  return changedFiles;
};

const parseWorktree = (repoRoot) => {
  const output = runGit(repoRoot, [
    "status",
    "--porcelain=v1",
    "--untracked-files=all",
  ]);
  return output
    .split(/\r?\n/)
    .filter((line) => line.length >= 3)
    .map((line) => {
      const indexCode = line[0];
      const worktreeCode = line[1];
      if (indexCode === "?" && worktreeCode === "?") {
        return { path: line.slice(3), status: "untracked" };
      }
      return pruneEmpty({
        path: line.slice(3),
        index:
          STATUS_NAMES[indexCode] === "unmodified"
            ? undefined
            : STATUS_NAMES[indexCode],
        worktree:
          STATUS_NAMES[worktreeCode] === "unmodified"
            ? undefined
            : STATUS_NAMES[worktreeCode],
      });
    });
};

const parseCommits = (repoRoot, mergeBase) => {
  const fields = runGit(repoRoot, [
    "log",
    "--reverse",
    "--format=%H%x00%s%x00",
    `${mergeBase}..HEAD`,
  ])
    .split("\0")
    .map((field) => field.trim())
    .filter(Boolean);
  const commits = [];
  for (let index = 0; index + 1 < fields.length; index += 2) {
    commits.push({ sha: fields[index], subject: fields[index + 1] });
  }
  return commits;
};

const parseDiffStats = (repoRoot, baseRef) =>
  runGit(repoRoot, [
    "diff",
    "--numstat",
    "--no-color",
    "--find-renames",
    `${baseRef}...HEAD`,
  ])
    .split(/\r?\n/)
    .filter(Boolean)
    .flatMap((line) => {
      const fields = line.split("\t", 3);
      if (fields.length !== 3) {
        return [];
      }
      const [additions, deletions, path] = fields;
      return [
        {
          path,
          additions: /^\d+$/.test(additions) ? Number(additions) : additions,
          deletions: /^\d+$/.test(deletions) ? Number(deletions) : deletions,
        },
      ];
    });

const diffBuffer = (repoRoot, baseRef, paths = []) => {
  const args = [
    "diff",
    "--binary",
    "--no-ext-diff",
    "--find-renames",
    `${baseRef}...HEAD`,
  ];
  if (paths.length > 0) {
    args.push("--", ...paths);
  }
  return runGit(repoRoot, args, { binary: true });
};

const buildSnapshot = (repoRoot, base) => {
  const { baseRef, baseSha, mergeBase } = normalizeBase(repoRoot, base);
  const changedFiles = parseChangedFiles(repoRoot, baseRef);
  return {
    repository: posix.basename(repoRoot),
    branch:
      runGit(repoRoot, ["branch", "--show-current"]).trim() || "detached-HEAD",
    head: runGit(repoRoot, ["rev-parse", "HEAD"]).trim(),
    base_ref: baseRef,
    base_sha: baseSha,
    merge_base: mergeBase,
    diff_hash: runGit(repoRoot, ["hash-object", "--stdin"], {
      input: diffBuffer(repoRoot, baseRef),
    }).trim(),
    changed_file_count: changedFiles.length,
    changed_files: changedFiles,
  };
};

const buildContext = (repoRoot, base) => {
  const snapshot = buildSnapshot(repoRoot, base);
  const moduleCounts = new Map();
  for (const entry of snapshot.changed_files) {
    moduleCounts.set(entry.module, (moduleCounts.get(entry.module) ?? 0) + 1);
  }
  return pruneEmpty({
    snapshot,
    worktree_changes: parseWorktree(repoRoot),
    commits: parseCommits(repoRoot, snapshot.merge_base),
    modules: [...moduleCounts]
      .sort(([left], [right]) => left.localeCompare(right))
      .map(([module, changedFiles]) => ({
        module,
        changed_files: changedFiles,
      })),
    diff_stats: parseDiffStats(repoRoot, snapshot.base_ref),
  });
};

function pruneEmpty(value) {
  if (Array.isArray(value)) {
    return value.map(pruneEmpty).filter(isEffective);
  }
  if (value && typeof value === "object") {
    return Object.fromEntries(
      Object.entries(value)
        .map(([key, item]) => [key, pruneEmpty(item)])
        .filter(([, item]) => isEffective(item)),
    );
  }
  return value;
}

const isEffective = (value) =>
  value !== undefined &&
  value !== null &&
  value !== "" &&
  (!Array.isArray(value) || value.length > 0) &&
  (typeof value !== "object" ||
    Array.isArray(value) ||
    Object.keys(value).length > 0);

const validatePaths = (paths) =>
  paths.map((rawPath) => {
    const normalized = posix.normalize(rawPath);
    if (
      isAbsolute(rawPath) ||
      normalized === ".." ||
      normalized.startsWith("../") ||
      rawPath.startsWith("-")
    ) {
      throw new Error(`path must be repository-relative: ${rawPath}`);
    }
    return normalized;
  });

const selectModulePaths = (changedFiles, modules) => {
  const requested = new Set(modules);
  const matchedModules = new Set();
  const paths = [];
  for (const entry of changedFiles) {
    if (!requested.has(entry.module)) {
      continue;
    }
    matchedModules.add(entry.module);
    if (entry.old_path) {
      paths.push(entry.old_path);
    }
    paths.push(entry.path);
  }
  const missing = [...requested].filter(
    (module) => !matchedModules.has(module),
  );
  if (missing.length > 0) {
    throw new Error(
      `module has no changed files: ${missing.sort().join(", ")}`,
    );
  }
  return paths;
};

const main = () => {
  const options = parseArgs(process.argv.slice(2));
  const repoRoot = findRepoRoot();
  if (options.command === "context") {
    console.log(JSON.stringify(buildContext(repoRoot, options.base), null, 2));
    return;
  }
  if (options.command === "snapshot") {
    console.log(JSON.stringify(buildSnapshot(repoRoot, options.base), null, 2));
    return;
  }

  let paths = [];
  if (options.modules.length > 0) {
    paths = selectModulePaths(
      parseChangedFiles(repoRoot, options.base),
      options.modules,
    );
  } else if (options.paths.length > 0) {
    paths = validatePaths(options.paths);
  }
  process.stdout.write(diffBuffer(repoRoot, options.base, paths));
};

try {
  main();
} catch (error) {
  console.error(`error: ${error.message}`);
  process.exitCode = 2;
}
