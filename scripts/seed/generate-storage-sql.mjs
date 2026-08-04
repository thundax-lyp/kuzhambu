#!/usr/bin/env node

import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const outputPath = resolve(repoRoot, "build/seed-sql/storage.sql");

mkdirSync(dirname(outputPath), { recursive: true });
writeFileSync(
  outputPath,
  "SET NAMES utf8mb4;\n\n-- Storage has no required seed data.\n",
);
