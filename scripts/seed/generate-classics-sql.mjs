#!/usr/bin/env node

import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");

const DEFAULT_SOURCE = resolve(repoRoot, "db/data-source/sancai-manuscripts.json");
const DEFAULT_OUTPUT = resolve(repoRoot, "db/data/classics.sql");
const DEFAULT_TAG_SEED = resolve(repoRoot, "db/data-source/sancai-tags.json");
const WANGQI_SOURCE = resolve(repoRoot, "db/data-source/wangqi-documents-full.json");
const MING_SOURCE = resolve(repoRoot, "db/data-source/ming-customs.json");

const usage = () => {
  console.log(`Usage:
  scripts/seed/generate-classics-sql.mjs [source_json] [output_sql] [tag_seed_json]

Defaults:
  source_json    db/data-source/sancai-manuscripts.json
  output_sql     db/data/classics.sql
  tag_seed_json  db/data-source/sancai-tags.json

Examples:
  scripts/seed/generate-classics-sql.mjs
  scripts/seed/generate-classics-sql.mjs db/data-source/sancai-manuscripts.json db/data/classics.sql
  scripts/seed/generate-classics-sql.mjs db/data-source/sancai-manuscripts.json db/data/classics.sql db/data-source/sancai-tags.json`);
};

const main = () => {
  const args = process.argv.slice(2);
  if (args[0] === "-h" || args[0] === "--help") {
    usage();
    return;
  }
  if (args.length > 3) {
    throw new Error(`unexpected argument '${args[3]}'`);
  }

  const sourcePath = resolvePath(args[0] ?? DEFAULT_SOURCE);
  const outputPath = resolvePath(args[1] ?? DEFAULT_OUTPUT);
  const tagSeedPath = resolvePath(args[2] ?? DEFAULT_TAG_SEED);

  const sancaiEntries = readJson(sourcePath);
  const tagSeed = readJson(tagSeedPath);
  const wangqiDocuments = readJson(WANGQI_SOURCE);
  const mingSeed = readJson(MING_SOURCE);

  validateSources(sancaiEntries, tagSeed, wangqiDocuments, mingSeed);

  const lines = [
    "SET NAMES utf8mb4;",
    "",
    "-- Seed data generated from structured JSON sources under db/data-source/.",
    "-- Sancai ids and priorities and Wangqi local ids are derived by this script.",
    "",
  ];
  const sancaiCounts = appendSancaiSql(lines, sancaiEntries, tagSeed);
  const wangqiCounts = appendWangqiSql(lines, wangqiDocuments, {
    tagPriorityOffset: sancaiCounts.tagCount,
    qaPriorityOffset: sancaiCounts.qaCount,
  });
  appendMingSql(lines, mingSeed.items, {
    tagPriorityOffset: sancaiCounts.tagCount + wangqiCounts.tagCount,
    qaPriorityOffset: sancaiCounts.qaCount + wangqiCounts.qaCount,
  });

  mkdirSync(dirname(outputPath), { recursive: true });
  writeFileSync(outputPath, `${lines.join("\n")}\n`);
  console.log(`generated: ${outputPath}`);
};

const resolvePath = (path) => resolve(process.cwd(), path);

const readJson = (path) => {
  try {
    return JSON.parse(readFileSync(path, "utf8"));
  } catch (error) {
    throw new Error(`failed to read JSON: ${path}: ${error.message}`);
  }
};

const validateSources = (sancaiEntries, tagSeed, wangqiDocuments, mingSeed) => {
  if (!Array.isArray(sancaiEntries)) {
    throw new Error("invalid source data format, expected manuscript array");
  }
  if (!sancaiEntries.every((entry) => entry.lifecycleStatus === "DRAFT")) {
    throw new Error("invalid Sancai lifecycleStatus");
  }
  if (
    !Array.isArray(wangqiDocuments) ||
    !wangqiDocuments.every(
      (document) =>
        document.lifecycleStatus === "DRAFT" &&
        typeof document.documentTime === "number",
    )
  ) {
    throw new Error("invalid Wangqi source data");
  }
  if (
    mingSeed?.schema !== "classics_ming_customs_seed" ||
    !Array.isArray(mingSeed.items) ||
    mingSeed.items.length === 0 ||
    !mingSeed.items.every(
      (item) =>
        item.lifecycleStatus === "DRAFT" &&
        typeof item.sourceRecordId === "number" &&
        typeof item.contentUpdatedAt === "number",
    )
  ) {
    throw new Error("invalid Ming customs source data");
  }
  if (tagSeed?.schema !== "classics_sancai_tag_seed") {
    throw new Error("invalid tag seed format, expected schema=classics_sancai_tag_seed");
  }
};

const appendSancaiSql = (lines, entries, tagSeed) => {
  const categories = buildCategories(entries);
  const volumes = buildVolumes(entries, categories);

  lines.push("-- 三才图会门类");
  for (const [categoryIndex, category] of categories.entries()) {
    lines.push(
      "INSERT INTO `classics_sancai_category` (`id`, `title`, `category_type`, `priority`) VALUES (" +
        `${categoryIndex + 1}, ` +
        `${sqlText(category.category)}, ` +
        `${sqlText(category.categoryType)}, ` +
        `${categoryIndex + 1}` +
        ") ON DUPLICATE KEY UPDATE " +
        "`title` = VALUES(`title`), `category_type` = VALUES(`category_type`), `priority` = VALUES(`priority`);",
    );
  }
  lines.push("");

  lines.push("-- 三才图会卷");
  for (const volume of volumes) {
    lines.push(
      "INSERT INTO `classics_sancai_volume` (`id`, `category_id`, `title`, `volume_type`, `priority`) VALUES (" +
        `${volume.volume_id}, ` +
        `${generatedCategoryId(categories, volume.category)}, ` +
        `${sqlText(volume.volume)}, ` +
        `${sqlText(volume.volumeType)}, ` +
        `${generatedVolumePriority(volume.volume_id)}` +
        ") ON DUPLICATE KEY UPDATE " +
        "`category_id` = VALUES(`category_id`), `title` = VALUES(`title`), `volume_type` = VALUES(`volume_type`), `priority` = VALUES(`priority`);",
    );
  }
  lines.push("");

  lines.push("-- 三才图会条目");
  for (const [entryIndex, entry] of entries.entries()) {
    lines.push(
      "INSERT INTO `classics_sancai_entry` (`id`, `volume_id`, `title`, `original_text`, `translation_text`, `summary`, `lifecycle_status`, `transition_status`, `current_publication_job_id`, `translation_status`, `image_status`, `visual_asset_status`, `refinement_status`, `priority`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
        `${generatedEntryId(entries, entryIndex, entry)}, ` +
        `${generatedVolumeId(volumes, entry.category, entry.volume)}, ` +
        `${sqlText(entry.title)}, ` +
        `${sqlText(entry.content)}, ` +
        `${sqlText(entry.translation)}, ` +
        `${sqlText(entry.summary)}, ` +
        `${sqlText(entry.lifecycleStatus ?? "DRAFT")}, ` +
        `${sqlText("NONE")}, ` +
        "NULL, " +
        `${sqlText((entry.translation ?? "") === "" ? "MISSING" : "READY")}, ` +
        `${sqlText("MISSING")}, ` +
        `${sqlText("MISSING")}, ` +
        `${sqlText("RAW")}, ` +
        `${entryIndex + 1}, ` +
        "NULL, " +
        "NULL, " +
        "NULL, " +
        sqlEpochMs("2026-01-01 00:00:00.000") +
        ") ON DUPLICATE KEY UPDATE " +
        "`volume_id` = VALUES(`volume_id`), `title` = VALUES(`title`), `original_text` = VALUES(`original_text`), `translation_text` = VALUES(`translation_text`), `summary` = VALUES(`summary`), `lifecycle_status` = VALUES(`lifecycle_status`), `transition_status` = VALUES(`transition_status`), `current_publication_job_id` = VALUES(`current_publication_job_id`), `translation_status` = VALUES(`translation_status`), `image_status` = VALUES(`image_status`), `visual_asset_status` = VALUES(`visual_asset_status`), `refinement_status` = VALUES(`refinement_status`), `priority` = VALUES(`priority`), `current_version_id` = VALUES(`current_version_id`), `current_version_no` = VALUES(`current_version_no`), `current_versioned_at` = VALUES(`current_versioned_at`), `content_updated_at` = VALUES(`content_updated_at`);",
    );
  }
  lines.push("");

  lines.push("-- 三才图会条目问答");
  lines.push("DELETE FROM `classics_content_qa_pair` WHERE `content_type` = 'SANCAI_ENTRY';");
  const qaRows = entries.flatMap((entry, entryIndex) =>
    (entry.qa ?? [])
      .filter((qa) => (qa.q ?? "") !== "" && (qa.a ?? "") !== "")
      .map((qa) => ({
        entry_id: generatedEntryId(entries, entryIndex, entry),
        qa,
      })),
  );
  for (const [qaIndex, row] of qaRows.entries()) {
    lines.push(
      "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
        `${sqlText("SANCAI_ENTRY")}, ` +
        `${row.entry_id}, ` +
        `${sqlText(row.qa.q)}, ` +
        `${sqlText(row.qa.a)}, ` +
        `${sqlText(row.qa.source ?? "MANUAL")}, ` +
        `${qaIndex + 1}` +
        ") ON DUPLICATE KEY UPDATE " +
        "`question` = VALUES(`question`), `answer` = VALUES(`answer`), `source` = VALUES(`source`), `priority` = VALUES(`priority`);",
    );
  }
  lines.push("");

  lines.push("-- 三才图会条目标签");
  const tagRows = entries.flatMap((entry, entryIndex) =>
    (entry.tags ?? []).map((tag) => ({
      entry_id: generatedEntryId(entries, entryIndex, entry),
      tag,
    })),
  );
  for (const [tagIndex, row] of tagRows.entries()) {
    lines.push(
      "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
        `${sqlText("SANCAI_ENTRY")}, ` +
        `${row.entry_id}, ` +
        `${generatedTagId(tagSeed, row.tag)}, ` +
        `${sqlText(row.tag)}, ` +
        `${sqlText("MANUAL")}, ` +
        `${sqlText("ACTIVE")}, ` +
        `${tagIndex + 1}` +
        ") ON DUPLICATE KEY UPDATE " +
        "`tag_id` = VALUES(`tag_id`), `source` = VALUES(`source`), `status` = VALUES(`status`), `priority` = VALUES(`priority`);",
    );
  }
  return {
    qaCount: qaRows.length,
    tagCount: tagRows.length,
  };
};

const appendWangqiSql = (lines, documents, offsets) => {
  const sortedDocuments = [...documents].sort((a, b) => a.id - b.id);

  lines.push(
    "",
    "-- 王圻文档",
    "-- Generated from db/data-source/wangqi-documents-full.json. Local document IDs are deterministic 1..14.",
    "DELETE FROM `classics_content_tag` WHERE `content_type` = 'WANGQI_DOCUMENT' AND `content_id` BETWEEN 1 AND 14;",
    "DELETE FROM `classics_content_qa_pair` WHERE `content_type` = 'WANGQI_DOCUMENT' AND `content_id` BETWEEN 1 AND 14;",
    "DELETE FROM `classics_content_version` WHERE `content_type` = 'WANGQI_DOCUMENT' AND `content_id` BETWEEN 1 AND 14;",
    "DELETE FROM `classics_wangqi_document_event` WHERE `document_id` BETWEEN 1 AND 14;",
    "DELETE FROM `classics_wangqi_document` WHERE `id` BETWEEN 1 AND 14;",
    "",
  );

  for (const [documentIndex, document] of sortedDocuments.entries()) {
    const localId = documentIndex + 1;
    lines.push(
      "INSERT INTO `classics_wangqi_document` (`id`, `title`, `summary`, `content_format`, `content`, `document_time`, `storage_object_id`, `lifecycle_status`, `transition_status`, `current_publication_job_id`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
        `${localId}, ` +
        `${sqlText(document.title)}, ` +
        `${sqlText(document.summary)}, ` +
        "'MARKDOWN', " +
        `${sqlText(document.content)}, ` +
        `${document.documentTime}, NULL, ` +
        `'${document.lifecycleStatus}', 'NONE', NULL, NULL, NULL, NULL, ` +
        `${document.documentTime});`,
    );
  }
  lines.push("");

  for (const [documentIndex, document] of sortedDocuments.entries()) {
    const localId = documentIndex + 1;
    lines.push(
      "INSERT INTO `classics_wangqi_document_event` (`id`, `document_id`, `title`, `occurred_at`, `occurred_label`, `summary`, `priority`) VALUES (" +
        `${localId}, ` +
        `${localId}, ` +
        `${sqlText(document.title)}, ` +
        `${document.documentTime}, ` +
        `${sqlText(document.eventOccurredLabel)}, ` +
        `${sqlText(document.summary)}, ` +
        `${localId});`,
    );
  }
  lines.push("");

  const tagRows = sortedDocuments.flatMap((document, documentIndex) =>
    (document.tags ?? []).map((tag) => ({
      content_id: documentIndex + 1,
      tag,
    })),
  );
  for (const [tagIndex, row] of tagRows.entries()) {
    lines.push(
      "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
        `${sqlText("WANGQI_DOCUMENT")}, ` +
        `${row.content_id}, ` +
        `${sqlText(row.tag)}, ` +
        `${sqlText("MANUAL")}, ` +
        `${sqlText("ACTIVE")}, ` +
        `${offsets.tagPriorityOffset + tagIndex + 1});`,
    );
  }
  lines.push("");

  const qaRows = sortedDocuments.flatMap((document, documentIndex) =>
    (document.qa_pairs ?? []).map((qa) => ({
      content_id: documentIndex + 1,
      qa,
    })),
  );
  for (const [qaIndex, row] of qaRows.entries()) {
    lines.push(
      "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
        `${sqlText("WANGQI_DOCUMENT")}, ` +
        `${row.content_id}, ` +
        `${sqlText(row.qa.question)}, ` +
        `${sqlText(row.qa.answer)}, ` +
        `${sqlText("MANUAL")}, ` +
        `${offsets.qaPriorityOffset + qaIndex + 1});`,
    );
  }
  lines.push(
    "",
    "ALTER TABLE `classics_wangqi_document` AUTO_INCREMENT = 15;",
    "ALTER TABLE `classics_wangqi_document_event` AUTO_INCREMENT = 15;",
  );
  return {
    qaCount: qaRows.length,
    tagCount: tagRows.length,
  };
};

const appendMingSql = (lines, entries, offsets) => {
  lines.push(
    "",
    "-- 明代习俗",
    "DELETE FROM `classics_content_tag` WHERE `content_type` = 'MING_CUSTOMS';",
    "DELETE FROM `classics_content_qa_pair` WHERE `content_type` = 'MING_CUSTOMS';",
    "DELETE FROM `classics_ming_customs_keyword`;",
    "DELETE FROM `classics_ming_customs_entry`;",
  );

  for (const [entryIndex, entry] of entries.entries()) {
    lines.push(
      "INSERT INTO `classics_ming_customs_entry` (`id`, `title`, `category`, `chapter`, `section`, `summary`, `content_format`, `content`, `original_excerpts`, `lifecycle_status`, `transition_status`, `current_publication_job_id`, `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`) VALUES (" +
        `${entryIndex + 1}, ` +
        `${sqlText(entry.title)}, ` +
        `${sqlText(entry.category)}, ` +
        `${sqlText(entry.chapter)}, ` +
        `${sqlText(entry.section)}, ` +
        `${sqlText(entry.summary)}, ` +
        `${sqlText(entry.contentFormat)}, ` +
        `${sqlText(entry.content)}, ` +
        `${sqlText(entry.originalExcerpts)}, ` +
        `${sqlText(entry.lifecycleStatus)}, ` +
        `${sqlText("NONE")}, NULL, NULL, NULL, NULL, ` +
        `${entry.contentUpdatedAt}` +
        ") ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `category` = VALUES(`category`), `chapter` = VALUES(`chapter`), `section` = VALUES(`section`), `summary` = VALUES(`summary`), `content_format` = VALUES(`content_format`), `content` = VALUES(`content`), `original_excerpts` = VALUES(`original_excerpts`), `lifecycle_status` = VALUES(`lifecycle_status`), `transition_status` = VALUES(`transition_status`), `current_publication_job_id` = VALUES(`current_publication_job_id`), `current_version_id` = VALUES(`current_version_id`), `current_version_no` = VALUES(`current_version_no`), `current_versioned_at` = VALUES(`current_versioned_at`), `content_updated_at` = VALUES(`content_updated_at`);",
    );
  }
  lines.push("");

  const mingEntryIdBySourceRecordId = new Map(
    entries.map((entry, entryIndex) => [entry.sourceRecordId, entryIndex + 1]),
  );
  const keywordRows = entries.flatMap((entry) =>
    (entry.keywords ?? []).map((keyword) => ({
      content_id: mingEntryIdBySourceRecordId.get(entry.sourceRecordId),
      keyword,
    })),
  );
  for (const [keywordIndex, row] of keywordRows.entries()) {
    lines.push(
      "INSERT INTO `classics_ming_customs_keyword` (`id`, `custom_id`, `keyword`, `priority`) VALUES (" +
        `${keywordIndex + 1}, ` +
        `${row.content_id}, ` +
        `${sqlText(row.keyword)}, ` +
        `${keywordIndex + 1}` +
        ") ON DUPLICATE KEY UPDATE `custom_id` = VALUES(`custom_id`), `keyword` = VALUES(`keyword`), `priority` = VALUES(`priority`);",
    );
  }
  lines.push("");

  const tagRows = entries.flatMap((entry) =>
    (entry.tags ?? []).map((tag) => ({
      content_id: mingEntryIdBySourceRecordId.get(entry.sourceRecordId),
      tag,
    })),
  );
  for (const [tagIndex, row] of tagRows.entries()) {
    lines.push(
      "INSERT INTO `classics_content_tag` (`content_type`, `content_id`, `tag_name_snapshot`, `source`, `status`, `priority`) VALUES (" +
        `${sqlText("MING_CUSTOMS")}, ` +
        `${row.content_id}, ` +
        `${sqlText(row.tag)}, ` +
        `${sqlText("MANUAL")}, ` +
        `${sqlText("ACTIVE")}, ` +
        `${offsets.tagPriorityOffset + tagIndex + 1});`,
    );
  }
  lines.push("");

  const qaRows = entries.flatMap((entry) =>
    (entry.qa ?? []).map((qa) => ({
      content_id: mingEntryIdBySourceRecordId.get(entry.sourceRecordId),
      qa,
    })),
  );
  for (const [qaIndex, row] of qaRows.entries()) {
    lines.push(
      "INSERT INTO `classics_content_qa_pair` (`content_type`, `content_id`, `question`, `answer`, `source`, `priority`) VALUES (" +
        `${sqlText("MING_CUSTOMS")}, ` +
        `${row.content_id}, ` +
        `${sqlText(row.qa.question)}, ` +
        `${sqlText(row.qa.answer)}, ` +
        `${sqlText("MANUAL")}, ` +
        `${offsets.qaPriorityOffset + qaIndex + 1});`,
    );
  }
};

const buildCategories = (entries) => {
  const categories = [];
  for (const entry of entries) {
    if (!categories.some((category) => category.category === entry.category)) {
      categories.push({
        category: entry.category,
        categoryType: entry.categoryType ?? "FORMAL",
      });
    }
  }
  return categories;
};

const buildVolumes = (entries, categories) => {
  const volumes = [];
  for (const entry of entries) {
    if (
      volumes.some(
        (volume) =>
          volume.category === entry.category && volume.volume === entry.volume,
      )
    ) {
      continue;
    }
    const categoryId = generatedCategoryId(categories, entry.category);
    volumes.push({
      category: entry.category,
      volume: entry.volume,
      volumeType: entry.volumeType ?? "MAIN",
      volume_id: volumes.length + 1,
    });
  }
  return volumes;
};

const generatedCategoryId = (categories, categoryName) =>
  categories.findIndex((category) => category.category === categoryName) + 1;

const generatedVolumeId = (volumes, categoryName, volumeName) =>
  volumes.find(
    (volume) => volume.category === categoryName && volume.volume === volumeName,
  )?.volume_id;

const generatedEntryId = (_entries, entryIndex, _entry) => entryIndex + 1;

const generatedTagId = (tagSeed, tag) => {
  const tagIndex = tagSeed.tags.findIndex((seedTag) => seedTag.name === tag);
  if (tagIndex < 0) {
    throw new Error(`Unknown Sancai manuscript tag: ${String(tag)}`);
  }
  return tagIndex + 1;
};

const generatedVolumePriority = (volumeId) => volumeId;

const sqlText = (value) => {
  if (value === null || value === undefined) {
    return "NULL";
  }
  const encoded = Buffer.from(String(value), "utf8").toString("base64");
  return `CAST(FROM_BASE64("${encoded}") AS CHAR CHARACTER SET utf8mb4)`;
};

const sqlEpochMs = (value) => {
  if (value === null || value === undefined) {
    return "NULL";
  }
  const raw = String(value).replace("T", " ");
  const match = raw.match(
    /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})(?:\.([0-9]{1,6}))?$/,
  );
  if (!match) {
    throw new Error(`Invalid Asia/Shanghai display datetime: ${raw}`);
  }
  const fraction = `${match[2] ?? ""}000000`.slice(0, 6);
  return `TIMESTAMPDIFF(MICROSECOND, '1970-01-01 08:00:00.000000', '${match[1]}.${fraction}') DIV 1000`;
};

main();
