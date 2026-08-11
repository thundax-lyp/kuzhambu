#!/usr/bin/env node

import {execFileSync} from "node:child_process";
import {extname, join, relative, resolve} from "node:path";

let input = "";

for await (const chunk of process.stdin) {
    input += chunk;
}

const data = JSON.parse(input);
const cwd = data.cwd ?? process.cwd();

const patch = data?.tool_input?.command ?? "";

// 提取 apply_patch 中涉及的文件
const files = [
    ...patch.matchAll(/^\*\*\* (?:Add|Update) File: (.+)$/gm),
].map((match) => match[1].trim());

if (files.length === 0) {
    process.exit(0);
}

const javaFiles = files
    .filter(
        (file) =>
            extname(file) === ".java" &&
            (file === "kuzhambu-servers" ||
                file.startsWith("kuzhambu-servers/")),
    )
    .map((file) => file.replace(/^kuzhambu-servers\//, ""));

const javaFilePattern = javaFiles
    .map((file) => resolve(cwd, "kuzhambu-servers", file))
    .map((file) => file.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"))
    .join("|");

const appFiles = files
    .filter((file) => file.startsWith("kuzhambu-apps/"))
    .map((file) => resolve(cwd, file));

const workerFiles = files
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
                cwd: join(cwd, "kuzhambu-servers"),
                stdio: "inherit",
            },
        );
    }

    const appFilesByPackage = new Map();

    for (const file of appFiles) {
        const packageRoot = file.split(`${join(cwd, "kuzhambu-apps")}/`)[1]?.split("/")[0];

        if (!packageRoot) {
            continue;
        }

        const packageFiles = appFilesByPackage.get(packageRoot) ?? [];
        packageFiles.push(relative(join(cwd, "kuzhambu-apps", packageRoot), file));
        appFilesByPackage.set(packageRoot, packageFiles);
    }

    for (const [packageRoot, packageFiles] of appFilesByPackage) {
        execFileSync("pnpm", ["exec", "prettier", "--write", ...packageFiles], {
            cwd: join(cwd, "kuzhambu-apps", packageRoot),
            stdio: "inherit",
        });
    }

    if (workerFiles.length > 0) {
        execFileSync(
            join(cwd, "kuzhambu-workers", ".venv", "bin", "python"),
            ["-m", "ruff", "format", ...workerFiles],
            {
                cwd: join(cwd, "kuzhambu-workers"),
                stdio: "inherit",
            },
        );
    }
} catch (error) {
    console.error("Formatting failed.");
    process.exit(error.status ?? 1);
}
