#!/usr/bin/env node

import { readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "..");
const sourcePath = resolve(repoRoot, "db/data-source/sancai-tags.json");
const outputPath = resolve(repoRoot, "db/data/knowledge.sql");
const GENERATED_REF_BASE = 531000;
const THEME_CATEGORY_ID = 1004;

const main = () => {
  const seed = readSeed(sourcePath);
  const tagIds = indexTags(seed.tags);
  const refs = buildRefs(seed.entries, tagIds);
  const sql = generateSql(seed.tags, seed.aliases, refs);

  if (process.argv.includes("--check")) {
    const current = readFileSync(outputPath, "utf8");
    if (current !== sql) {
      console.error(
        "db/data/knowledge.sql is out of date. Run: node scripts/generate-sancai-knowledge-data-sql.mjs",
      );
      process.exit(1);
    }
    return;
  }

  writeFileSync(outputPath, sql);
};

const readSeedFile = (path) => {
  const seed = JSON.parse(readFileSync(path, "utf8"));
  if (seed.schema !== "classics_sancai_tag_seed") {
    throw new Error(
      "Invalid seed format, expected schema=classics_sancai_tag_seed",
    );
  }
  return seed;
};

const readSeed = (path) => {
  const seed = readSeedFile(path);
  validateSeed(seed);
  return seed;
};

const validateSeed = (seed) => {
  if (!Array.isArray(seed.tags)) {
    throw new Error("sancai tag seed must contain tags array");
  }
  if (!Array.isArray(seed.entries)) {
    throw new Error("sancai tag seed must contain entries array");
  }
  if (!Array.isArray(seed.aliases)) {
    throw new Error("sancai tag seed must contain aliases array");
  }

  assertUnique(
    seed.tags.map((tag) => tag.tag_id),
    "tag_id",
  );
  assertUnique(
    seed.tags.map((tag) => tag.name),
    "tag name",
  );
  assertUnique(
    seed.aliases.map((alias) => alias.alias_id),
    "alias_id",
  );

  const tagNames = new Set(seed.tags.map((tag) => tag.name));
  for (const entry of seed.entries) {
    if (!Number.isInteger(entry.content_id)) {
      throw new Error(`Invalid content_id: ${entry.content_id}`);
    }
    requireNonBlank(entry.content_title, `entry ${entry.content_id} title`);
    if (!Array.isArray(entry.tags)) {
      throw new Error(`Invalid tags for entry ${entry.content_id}`);
    }
    for (const tagName of entry.tags) {
      if (!tagNames.has(tagName)) {
        throw new Error(`Unknown tag "${tagName}" on entry ${entry.content_id}`);
      }
    }
  }
};

const indexTags = (tags) => {
  const tagIds = new Map();
  for (const tag of tags) {
    const tagId = tag.tag_id;
    if ([...tagIds.values()].includes(tagId)) {
      throw new Error(`Duplicate tag id: ${tagId}`);
    }
    tagIds.set(tag.name, tagId);
  }
  return tagIds;
};

const buildRefs = (entries, tagIds) => {
  const refs = [];
  let refIndex = 1;
  for (const entry of entries) {
    for (const tag of entry.tags) {
      refs.push({
        refId: GENERATED_REF_BASE + refIndex++,
        tagId: requireLookup(tagIds, tag, "tag"),
        contentType: "SANCAI_ENTRY",
        contentId: entry.content_id,
        contentTitle: entry.content_title,
      });
    }
  }
  return refs;
};

const generateSql = (tags, aliases, refs) => {
  const lines = ["SET NAMES utf8mb4;", ""];
  appendTagCategorySql(lines);
  appendTagSql(lines, tags);
  appendTagAliasSql(lines, aliases);
  appendTagContentRefSql(lines, refs);
  while (lines.at(-1) === "") {
    lines.pop();
  }
  return `${lines.join("\n")}\n`;
};

const appendTagCategorySql = (lines) => {
  lines.push("INSERT INTO `knowledge_tag_category` (");
  lines.push("    `category_id`, `name`, `description`, `priority`, `status`");
  lines.push(") VALUES");
  lines.push(
    [
      row([1001, "人物", "人物类别", 10, "ENABLED"]),
      row([1002, "地点", "地理地点", 20, "ENABLED"]),
      row([1003, "时代", "历史时代", 30, "ENABLED"]),
      row([1004, "主题", "主题分类", 40, "ENABLED"]),
    ].join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `description` = VALUES(`description`),");
  lines.push("    `priority` = VALUES(`priority`),");
  lines.push("    `status` = VALUES(`status`);");
  lines.push("");
};

const appendTagSql = (lines, tags) => {
  if (tags.length === 0) {
    return;
  }
  lines.push("-- 三才图会标签库种子，来源：db/data-source/sancai-tags.json。");
  lines.push("INSERT INTO `knowledge_tag` (");
  lines.push("    `tag_id`, `name`, `category_id`, `description`, `status`, `source`,");
  lines.push("    `review_status`, `review_note`, `created_at`, `reviewed_at`, `merged_to_tag_id`,");
  lines.push("    `deprecated_at`, `deprecated_by`");
  lines.push(") VALUES");
  lines.push(
    tags
      .map((tag) =>
        row([
          tag.tag_id,
          tag.name,
          tag.category_id ?? THEME_CATEGORY_ID,
          tag.description,
          tag.status,
          tag.source,
          tag.review_status,
          tag.review_note,
          tag.created_at,
          tag.reviewed_at,
          null,
          null,
          null,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `category_id` = VALUES(`category_id`),");
  lines.push("    `description` = VALUES(`description`),");
  lines.push("    `status` = VALUES(`status`),");
  lines.push("    `source` = VALUES(`source`),");
  lines.push("    `review_status` = VALUES(`review_status`),");
  lines.push("    `review_note` = VALUES(`review_note`),");
  lines.push("    `created_at` = VALUES(`created_at`),");
  lines.push("    `reviewed_at` = VALUES(`reviewed_at`),");
  lines.push("    `merged_to_tag_id` = VALUES(`merged_to_tag_id`),");
  lines.push("    `deprecated_at` = VALUES(`deprecated_at`),");
  lines.push("    `deprecated_by` = VALUES(`deprecated_by`);");
  lines.push("");
};

const appendTagAliasSql = (lines, aliases) => {
  if (aliases.length === 0) {
    return;
  }
  lines.push("INSERT INTO `knowledge_tag_alias` (");
  lines.push("    `alias_id`, `tag_id`, `name`, `source`");
  lines.push(") VALUES");
  lines.push(
    aliases
      .map((alias) =>
        row([alias.alias_id, alias.tag_id, alias.name, alias.source]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `tag_id` = VALUES(`tag_id`),");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `source` = VALUES(`source`);");
  lines.push("");
};

const appendTagContentRefSql = (lines, refs) => {
  if (refs.length === 0) {
    return;
  }
  lines.push("-- 三才图会内容标签引用投影，来源：db/data-source/sancai-tags.json。");
  lines.push("INSERT INTO `knowledge_tag_content_ref` (");
  lines.push("    `ref_id`, `tag_id`, `content_type`, `content_id`, `content_title`, `source`");
  lines.push(") VALUES");
  lines.push(
    refs
      .map((ref) =>
        row([
          ref.refId,
          ref.tagId,
          ref.contentType,
          ref.contentId,
          ref.contentTitle,
          "MANUAL",
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `tag_id` = VALUES(`tag_id`),");
  lines.push("    `content_type` = VALUES(`content_type`),");
  lines.push("    `content_id` = VALUES(`content_id`),");
  lines.push("    `content_title` = VALUES(`content_title`),");
  lines.push("    `source` = VALUES(`source`);");
  lines.push("");
};

const row = (values) => `    (${values.map(sqlValue).join(", ")})`;

const sqlValue = (value) => {
  if (value == null) {
    return "NULL";
  }
  if (typeof value === "number") {
    return String(value);
  }
  return `'${String(value).replace(/'/g, "''")}'`;
};

const requireLookup = (map, key, type) => {
  const value = map.get(key);
  if (value == null) {
    throw new Error(`Unknown ${type}: ${key}`);
  }
  return value;
};

const requireNonBlank = (value, field) => {
  const text = String(value ?? "").trim();
  if (!text) {
    throw new Error(`Missing ${field}`);
  }
  return text;
};

const assertUnique = (values, field) => {
  const seen = new Set();
  for (const value of values) {
    if (seen.has(value)) {
      throw new Error(`Duplicate ${field}: ${value}`);
    }
    seen.add(value);
  }
};

main();
