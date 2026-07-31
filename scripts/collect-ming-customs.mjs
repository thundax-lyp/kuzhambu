#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT_DIR = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const PAGE_URL = "https://ptrmt-beta.aisn.tech/sancai/ming_customs.html";
const API_URL = "https://ptrmt-beta.aisn.tech/sancai/ming/customs";
const OUTPUT = path.join(ROOT_DIR, "db/data-source/ming-customs.json");

function parseArray(value, field, sourceRecordId) {
  if (Array.isArray(value)) {
    return value;
  }
  if (typeof value === "string") {
    const parsed = JSON.parse(value);
    if (Array.isArray(parsed)) {
      return parsed;
    }
  }
  throw new Error(`Record ${sourceRecordId} has invalid ${field}`);
}

function parseShanghaiEpochMs(value, sourceRecordId) {
  const match = String(value ?? "").match(
    /^(\d{4})[/-](\d{1,2})[/-](\d{1,2})[ T](\d{1,2}):(\d{2}):(\d{2})$/,
  );
  if (!match) {
    throw new Error(`Record ${sourceRecordId} has invalid updated_at`);
  }
  const [, year, month, day, hour, minute, second] = match;
  return Date.parse(
    `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}T${hour.padStart(2, "0")}:${minute}:${second}+08:00`,
  );
}

function normalizeCategory(value) {
  const category = String(value ?? "").trim();
  if (category.startsWith("食")) return "食（饮食生活）";
  if (category.startsWith("衣")) return "衣（服饰穿戴）";
  if (category.startsWith("住")) return "住（居住空间）";
  if (category.startsWith("行")) return "行（出行与行旅）";
  throw new Error(`Unsupported Ming customs category: ${category}`);
}

function normalizeRecord(record) {
  const sourceRecordId = Number(record.id);
  if (!Number.isInteger(sourceRecordId) || sourceRecordId <= 0) {
    throw new Error(`Invalid source record id: ${record.id}`);
  }
  const titleMatch = String(record.content ?? "").match(/^【([^】]+)】/);
  if (!titleMatch) {
    throw new Error(`Record ${sourceRecordId} has no bracketed title`);
  }

  return {
    sourceRecordId,
    sourceId: record.original_id,
    batch: record.batch,
    title: titleMatch[1].trim(),
    category: normalizeCategory(record.category),
    chapter: record.chapter,
    section: record.section,
    summary: record.summary,
    contentFormat: "MARKDOWN",
    content: record.content,
    originalExcerpts: record.original_excerpts,
    lifecycleStatus: "DRAFT",
    contentUpdatedAt: parseShanghaiEpochMs(record.updated_at, sourceRecordId),
    keywords: parseArray(record.keywords, "keywords", sourceRecordId),
    tags: parseArray(record.tags, "tags", sourceRecordId),
    qa: parseArray(record.qa_pairs, "qa_pairs", sourceRecordId).map((item) => ({
      question: item.q,
      answer: item.a,
    })),
  };
}

const response = await fetch(API_URL, {
  headers: { Accept: "application/json" },
  signal: AbortSignal.timeout(60_000),
});
if (!response.ok) {
  throw new Error(`Ming customs request failed: HTTP ${response.status}`);
}

const sourceRecords = await response.json();
if (!Array.isArray(sourceRecords) || sourceRecords.length === 0) {
  throw new Error("Ming customs response must be a non-empty array");
}

const items = sourceRecords.map(normalizeRecord).sort((a, b) => a.sourceRecordId - b.sourceRecordId);
if (new Set(items.map((item) => item.sourceRecordId)).size !== items.length) {
  throw new Error("Ming customs source record ids must be unique");
}

const output = {
  schema: "classics_ming_customs_seed",
  sourcePage: PAGE_URL,
  sourceApi: API_URL,
  items,
};

await fs.writeFile(OUTPUT, `${JSON.stringify(output, null, 2)}\n`, "utf8");
console.log(`collected: ${OUTPUT} (${items.length} records)`);
