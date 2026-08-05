#!/usr/bin/env node

import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const tagSourcePath = resolve(repoRoot, "db/data-source/sancai-tags.json");
const manuscriptSourcePath = resolve(
  repoRoot,
  "db/data-source/sancai-manuscripts.json",
);
const outputPath = resolve(repoRoot, "build/seed-sql/knowledge.sql");
const THEME_CATEGORY_ID = 4;
const DEFAULT_AI_CATEGORY_ID = 1999;
const MYSQL_EPOCH_SHANGHAI = "1970-01-01 08:00:00.000000";

const main = () => {
  const seed = readSeed(tagSourcePath);
  const manuscripts = readManuscripts(manuscriptSourcePath);
  const tagIds = buildTagIds(seed.tags);
  const refs = buildRefs(manuscripts, tagIds);
  const sql = generateSql(seed.tags, seed.aliases, refs, tagIds);

  if (process.argv.includes("--check")) {
    const current = readFileSync(outputPath, "utf8");
    if (current !== sql) {
      console.error(
        "build/seed-sql/knowledge.sql is out of date. Run: node scripts/seed/generate-sancai-knowledge-sql.mjs",
      );
      process.exit(1);
    }
    return;
  }

  mkdirSync(dirname(outputPath), { recursive: true });
  writeFileSync(outputPath, sql);
};

const readSeed = (path) => {
  const seed = JSON.parse(readFileSync(path, "utf8"));
  if (seed.schema !== "classics_sancai_tag_seed") {
    throw new Error(
      "Invalid seed format, expected schema=classics_sancai_tag_seed",
    );
  }
  validateSeed(seed);
  return seed;
};

const readManuscripts = (path) => {
  const manuscripts = JSON.parse(readFileSync(path, "utf8"));
  if (!Array.isArray(manuscripts)) {
    throw new Error("sancai manuscript source must be an array");
  }
  validateManuscripts(manuscripts);
  return manuscripts;
};

const validateSeed = (seed) => {
  if (!Array.isArray(seed.tags)) {
    throw new Error("sancai tag seed must contain tags array");
  }
  if (!Array.isArray(seed.aliases)) {
    throw new Error("sancai tag seed must contain aliases array");
  }

  assertUnique(
    seed.tags.map((tag) => tag.name),
    "tag name",
  );

  const tagNames = new Set(seed.tags.map((tag) => tag.name));
  for (const alias of seed.aliases) {
    requireNonBlank(alias.name, "alias name");
    requireNonBlank(alias.target, `alias ${alias.name} target`);
    if (!tagNames.has(alias.target)) {
      throw new Error(`Unknown alias target "${alias.target}"`);
    }
  }
};

const validateManuscripts = (manuscripts) => {
  manuscripts.forEach((entry, index) => {
    requireNonBlank(entry.category, `manuscript ${index + 1} category`);
    requireNonBlank(entry.volume, `manuscript ${index + 1} volume`);
    requireNonBlank(entry.title, `manuscript ${index + 1} title`);
    requireNonBlank(entry.content, `manuscript ${index + 1} content`);
    if (!Array.isArray(entry.tags)) {
      throw new Error(`Invalid tags for manuscript ${index + 1}`);
    }
    if (!Array.isArray(entry.qa)) {
      throw new Error(`Invalid qa for manuscript ${index + 1}`);
    }
    entry.qa.forEach((qa, qaIndex) => {
      requireNonBlank(qa.q, `manuscript ${index + 1} qa ${qaIndex + 1} q`);
      requireNonBlank(qa.a, `manuscript ${index + 1} qa ${qaIndex + 1} a`);
    });
  });
};

const buildTagIds = (tags) => {
  return new Map(tags.map((tag, index) => [tag.name, index + 1]));
};

const buildRefs = (manuscripts, tagIds) => {
  const refs = [];
  let refIndex = 1;
  manuscripts.forEach((entry, entryIndex) => {
    for (const tag of entry.tags) {
      refs.push({
        refId: refIndex++,
        tagId: requireLookup(tagIds, tag, "tag"),
        contentType: "SANCAI_ENTRY",
        contentId: entryIndex + 1,
        contentTitle: entry.title,
      });
    }
  });
  return refs;
};

const generateSql = (tags, aliases, refs, tagIds) => {
  const lines = ["SET NAMES utf8mb4;", ""];
  appendTagCategorySql(lines);
  appendTagSql(lines, tags, tagIds);
  appendTagAliasSql(lines, aliases, tagIds);
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
      row([1, "人物", "人物类别", 1, "ENABLED"]),
      row([2, "地点", "地理地点", 2, "ENABLED"]),
      row([3, "时代", "历史时代", 3, "ENABLED"]),
      row([4, "主题", "主题分类", 4, "ENABLED"]),
      row([
        DEFAULT_AI_CATEGORY_ID,
        "AI候选",
        "AI 生成标签默认分类",
        DEFAULT_AI_CATEGORY_ID,
        "ENABLED",
      ]),
    ].join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `description` = VALUES(`description`),");
  lines.push("    `priority` = VALUES(`priority`),");
  lines.push("    `status` = VALUES(`status`);");
  lines.push("");
};

const appendTagSql = (lines, tags, tagIds) => {
  if (tags.length === 0) {
    return;
  }
  lines.push("-- 三才图会标签库种子，来源：db/data-source/sancai-tags.json。");
  lines.push("INSERT INTO `knowledge_tag` (");
  lines.push(
    "    `tag_id`, `name`, `category_id`, `description`, `status`, `source`,",
  );
  lines.push(
    "    `review_status`, `review_note`, `created_at`, `reviewed_at`, `merged_to_tag_id`,",
  );
  lines.push("    `deprecated_at`, `deprecated_by`");
  lines.push(") VALUES");
  lines.push(
    tags
      .map((tag) =>
        row([
          requireLookup(tagIds, tag.name, "tag"),
          tag.name,
          THEME_CATEGORY_ID,
          tag.description,
          tag.status,
          normalizeTagSource(tag.source),
          tag.reviewStatus,
          tag.reviewNote,
          epochMillis(tag.createdAt),
          epochMillis(tag.reviewedAt),
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

const appendTagAliasSql = (lines, aliases, tagIds) => {
  if (aliases.length === 0) {
    return;
  }
  lines.push("INSERT INTO `knowledge_tag_alias` (");
  lines.push("    `alias_id`, `tag_id`, `name`, `source`");
  lines.push(") VALUES");
  lines.push(
    aliases
      .map((alias, index) =>
        row([
          index + 1,
          requireLookup(tagIds, alias.target, "tag"),
          alias.name,
          normalizeTagSource(alias.source),
        ]),
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
  lines.push(
    "-- 三才图会内容标签引用投影，来源：db/data-source/sancai-manuscripts.json。",
  );
  lines.push("INSERT INTO `knowledge_tag_content_ref` (");
  lines.push(
    "    `ref_id`, `tag_id`, `content_type`, `content_id`, `content_title`, `source`",
  );
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
  if (isRawSql(value)) {
    return value.rawSql;
  }
  return `'${String(value).replace(/'/g, "''")}'`;
};

const normalizeTagSource = (source) => {
  const value = String(source ?? "MANUAL").trim().toUpperCase();
  return value === "SEED" ? "MANUAL" : value;
};

const epochMillis = (displayTime) => ({
  rawSql: `TIMESTAMPDIFF(MICROSECOND, '${MYSQL_EPOCH_SHANGHAI}', '${normalizeDisplayTime(displayTime)}') DIV 1000`,
});

const normalizeDisplayTime = (displayTime) => {
  const text = String(displayTime ?? "").trim().replace("T", " ");
  const match = text.match(
    /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})(?:\.(\d{1,6}))?$/,
  );
  if (!match) {
    throw new Error(`Invalid Asia/Shanghai display datetime: ${displayTime}`);
  }
  return `${match[1]}.${(match[2] ?? "").padEnd(6, "0")}`;
};

const isRawSql = (value) =>
  typeof value === "object" &&
  value !== null &&
  typeof value.rawSql === "string";

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
