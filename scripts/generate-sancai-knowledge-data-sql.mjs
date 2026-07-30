#!/usr/bin/env node

import { readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "..");
const sourcePath = resolve(repoRoot, "sancai_tree_snapshot.json");
const outputPath = resolve(repoRoot, "db/data/knowledge.sql");
const GENERATED_TAG_BASE = 501000;
const GENERATED_REF_BASE = 531000;
const THEME_CATEGORY_ID = 1004;
const SEED_AT = "2026-07-30 00:00:00.000";
const LEGACY_TAG_IDS = new Map([["世系图", 500001]]);

const main = () => {
  const snapshot = readSnapshot(sourcePath);
  const entries = readEntries(snapshot);
  const tagNames = uniqueSorted(
    entries.flatMap((entry) => entry.tags.map((tag) => tag.name)),
  );
  const tagIds = assignTagIds(tagNames);
  const refs = buildRefs(entries, tagIds);
  const sql = generateSql(tagNames, tagIds, refs);

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

const readSnapshot = (path) => {
  const snapshot = JSON.parse(readFileSync(path, "utf8"));
  if (snapshot.schema !== "classics_sancai_tree") {
    throw new Error("Invalid snapshot format, expected schema=classics_sancai_tree");
  }
  return snapshot;
};

const readEntries = (snapshot) => {
  const entries = [];
  for (const category of snapshot.categories ?? []) {
    for (const volume of category.volumes ?? []) {
      for (const entry of volume.entries ?? []) {
        const entryId = entry.id ?? entry.entry_id;
        if (!Number.isInteger(entryId)) {
          throw new Error(`Invalid entry id for title: ${entry.title ?? ""}`);
        }
        const tags = parseTags(entry.tags_snapshot).map((name) => ({ name }));
        entries.push({
          id: entryId,
          title: requireNonBlank(entry.title, `entry ${entryId} title`),
          tags,
        });
      }
    }
  }
  return entries;
};

const parseTags = (value) => {
  if (value == null) {
    return [];
  }
  const rawTags = typeof value === "string" ? JSON.parse(value) : value;
  if (!Array.isArray(rawTags)) {
    throw new Error("tags_snapshot must be a JSON array or a stringified JSON array");
  }
  return uniquePreserved(rawTags.map((tag) => String(tag).trim()).filter(Boolean));
};

const assignTagIds = (tagNames) => {
  const tagIds = new Map();
  for (const [index, name] of tagNames.entries()) {
    const tagId = LEGACY_TAG_IDS.get(name) ?? GENERATED_TAG_BASE + index + 1;
    if ([...tagIds.values()].includes(tagId)) {
      throw new Error(`Duplicate tag id: ${tagId}`);
    }
    tagIds.set(name, tagId);
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
        tagId: requireLookup(tagIds, tag.name, "tag"),
        contentType: "SANCAI_ENTRY",
        contentId: entry.id,
        contentTitle: entry.title,
      });
    }
  }
  return refs;
};

const generateSql = (tagNames, tagIds, refs) => {
  const lines = ["SET NAMES utf8mb4;", ""];
  appendTagCategorySql(lines);
  appendTagSql(lines, tagNames, tagIds);
  appendTagAliasSql(lines);
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

const appendTagSql = (lines, tagNames, tagIds) => {
  if (tagNames.length === 0) {
    return;
  }
  lines.push("-- 三才图会标签库种子，来源：sancai_tree_snapshot.json tags_snapshot。");
  lines.push("INSERT INTO `knowledge_tag` (");
  lines.push("    `tag_id`, `name`, `category_id`, `description`, `status`, `source`,");
  lines.push("    `review_status`, `review_note`, `created_at`, `reviewed_at`, `merged_to_tag_id`,");
  lines.push("    `deprecated_at`, `deprecated_by`");
  lines.push(") VALUES");
  lines.push(
    tagNames
      .map((name) =>
        row([
          requireLookup(tagIds, name, "tag"),
          name,
          THEME_CATEGORY_ID,
          descriptionFor(name),
          "ENABLED",
          sourceFor(name),
          "APPROVED",
          reviewNoteFor(name),
          createdAtFor(name),
          reviewedAtFor(name),
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

const appendTagAliasSql = (lines) => {
  lines.push("INSERT INTO `knowledge_tag_alias` (");
  lines.push("    `alias_id`, `tag_id`, `name`, `source`");
  lines.push(") VALUES");
  lines.push(row([510001, 500001, "世系图谱", "MANUAL"]));
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
  lines.push("-- 三才图会内容标签引用投影，来源：sancai_tree_snapshot.json tags_snapshot。");
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

const descriptionFor = (name) =>
  name === "世系图"
    ? "用于知识问答和跨库检索的世系图主题标签"
    : "从三才图会稿件标签快照导入的主题标签";

const sourceFor = (name) => (name === "世系图" ? "MANUAL" : "SEED");

const reviewNoteFor = (name) =>
  name === "世系图" ? "联通 Discovery 查询理解与来源引用" : "三才图会快照导入";

const createdAtFor = (name) =>
  name === "世系图" ? "2026-02-27 04:00:00.000" : SEED_AT;

const reviewedAtFor = createdAtFor;

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

const uniqueSorted = (values) => [...new Set(values)].sort(compareText);

const uniquePreserved = (values) => [...new Set(values)];

const compareText = (left, right) => (left < right ? -1 : left > right ? 1 : 0);

main();
