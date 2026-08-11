#!/usr/bin/env node

import {execFileSync} from "node:child_process";
import {fileURLToPath} from "node:url";
import {dirname, extname, join, relative, resolve} from "node:path";

let input = "";

for await (const chunk of process.stdin) {
    input += chunk;
}

const data = JSON.parse(input);
const sourceCwd = data.cwd ?? process.cwd();
const repositoryRoot = resolve(dirname(fileURLToPath(import.meta.url)), "../..");

const toolInput = data?.tool_input;
const patch = typeof toolInput === "string" ? toolInput : toolInput?.command ?? "";

// 提取 apply_patch 中涉及的文件
const files = [];
let activeFile;

for (const line of patch.split("\n")) {
    const fileMatch = line.match(/^\*\*\* (?:Add|Update) File: (.+)$/);

    if (fileMatch) {
        if (activeFile) {
            files.push(activeFile);
        }
        activeFile = fileMatch[1].trim();
        continue;
    }

    const moveMatch = line.match(/^\*\*\* Move to: (.+)$/);
    if (moveMatch && activeFile) {
        activeFile = moveMatch[1].trim();
        continue;
    }

    if (line.startsWith("*** ") && activeFile) {
        files.push(activeFile);
        activeFile = undefined;
    }
}

if (activeFile) {
    files.push(activeFile);
}

const repositoryFiles = files
    .map((file) => {
        const absolutePath = file.startsWith("/")
            ? resolve(file)
            : file.startsWith("kuzhambu-") || file.startsWith(".codex/")
              ? resolve(repositoryRoot, file)
              : resolve(sourceCwd, file);
        const repositoryPath = relative(repositoryRoot, absolutePath);
        return repositoryPath.startsWith("..") ? null : repositoryPath;
    })
    .filter((file) => file !== null);

if (repositoryFiles.length === 0) {
    process.exit(0);
}

const javaFiles = repositoryFiles
    .filter(
        (file) =>
            extname(file) === ".java" &&
            (file === "kuzhambu-servers" ||
                file.startsWith("kuzhambu-servers/")),
    )
    .map((file) => resolve(repositoryRoot, file));

const javaFilePattern = javaFiles
    .map((file) => file.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"))
    .join("|");

const appFiles = repositoryFiles
    .filter((file) => file.startsWith("kuzhambu-apps/"))
    .map((file) => resolve(repositoryRoot, file));

const workerFiles = repositoryFiles
    .filter(
        (file) =>
            extname(file) === ".py" && file.startsWith("kuzhambu-workers/"),
    )
    .map((file) => file.replace(/^kuzhambu-workers\//, ""));

try {
    if (javaFilePattern) {
        execFileSync(
            "mvn",
            ["spotless:apply", `-DspotlessFiles=${javaFilePattern}`],
            {
                cwd: join(repositoryRoot, "kuzhambu-servers"),
                stdio: "inherit",
            },
        );
    }

    const appFilesByPackage = new Map();

    for (const file of appFiles) {
        const packageRoot = file.split(`${join(repositoryRoot, "kuzhambu-apps")}/`)[1]?.split("/")[0];

        if (!packageRoot) {
            continue;
        }

        const packageFiles = appFilesByPackage.get(packageRoot) ?? [];
        packageFiles.push(relative(join(repositoryRoot, "kuzhambu-apps", packageRoot), file));
        appFilesByPackage.set(packageRoot, packageFiles);
    }

    for (const [packageRoot, packageFiles] of appFilesByPackage) {
        execFileSync("pnpm", ["exec", "prettier", "--write", ...packageFiles], {
            cwd: join(repositoryRoot, "kuzhambu-apps", packageRoot),
            stdio: "inherit",
        });
    }

    if (workerFiles.length > 0) {
        execFileSync(
            join(repositoryRoot, "kuzhambu-workers", ".venv", "bin", "python"),
            ["-m", "ruff", "format", ...workerFiles],
            {
                cwd: join(repositoryRoot, "kuzhambu-workers"),
                stdio: "inherit",
            },
        );
    }
} catch (error) {
    console.error("Formatting failed.");
    process.exit(error.status ?? 1);
}
