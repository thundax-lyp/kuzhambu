#!/usr/bin/env node

import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const sourcePath = resolve(repoRoot, "db/data-source/test/wangqi-ai-smoke.json");
const outputPath = resolve(repoRoot, "build/seed-sql/test.sql");
const MYSQL_EPOCH_SHANGHAI = "1970-01-01 08:00:00.000000";

const main = () => {
  const seed = readSeed(sourcePath);
  const sql = generate(seed);
  if (process.argv.includes("--check")) {
    const current = readFileSync(outputPath, "utf8");
    if (current !== sql) {
      console.error(
        "build/seed-sql/test.sql is out of date. Run: node scripts/seed/generate-test-sql.mjs",
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
  if (seed.schema !== "kuzhambu_test_wangqi_ai_smoke_seed") {
    throw new Error("Invalid test seed schema");
  }
  validateSeed(seed);
  return seed;
};

const validateSeed = (seed) => {
  const { idRange, document, aiRuns } = seed;
  assertPositiveInteger(idRange?.start, "idRange.start");
  assertPositiveInteger(idRange?.end, "idRange.end");
  assertPositiveInteger(idRange?.nextAutoIncrement, "idRange.nextAutoIncrement");
  assertInRange(document?.id, idRange, "document.id");
  assertNonBlank(document.title, "document.title");
  assertNonBlank(document.contentFormat, "document.contentFormat");
  assertNonBlank(document.lifecycleStatus, "document.lifecycleStatus");
  assertPositiveInteger(document.version?.id, "document.version.id");
  assertPositiveInteger(document.event?.id, "document.event.id");
  for (const [index, tag] of (document.tags ?? []).entries()) {
    assertInRange(tag.id, idRange, `document.tags[${index}].id`);
    assertNonBlank(tag.name, `document.tags[${index}].name`);
  }
  for (const [index, qa] of (document.qaPairs ?? []).entries()) {
    assertInRange(qa.id, idRange, `document.qaPairs[${index}].id`);
    assertNonBlank(qa.question, `document.qaPairs[${index}].question`);
    assertNonBlank(qa.answer, `document.qaPairs[${index}].answer`);
  }
  if (!Array.isArray(aiRuns) || aiRuns.length === 0) {
    throw new Error("aiRuns must be a non-empty array");
  }
  for (const [index, run] of aiRuns.entries()) {
    assertInRange(run.id, idRange, `aiRuns[${index}].id`);
    assertNonBlank(run.capability, `aiRuns[${index}].capability`);
    assertNonBlank(run.resultFormat, `aiRuns[${index}].resultFormat`);
    assertNonBlank(run.modelName, `aiRuns[${index}].modelName`);
  }
};

const generate = (seed) => {
  const lines = [
    "SET NAMES utf8mb4;",
    "",
    "-- Dev/test-only data. Do not import this file into production environments.",
    `-- Generated from db/data-source/test/wangqi-ai-smoke.json.`,
    `-- Fixed ID range: ${seed.idRange.start}-${seed.idRange.end}.`,
    "-- The delete block keeps this file idempotent for repeated local/dev imports.",
    "",
  ];
  appendDeleteSql(lines, seed);
  appendDocumentSql(lines, seed.document);
  appendDocumentEventSql(lines, seed.document);
  appendContentVersionSql(lines, seed.document);
  appendContentTagSql(lines, seed.document);
  appendContentQaSql(lines, seed.document);
  appendAiBatchJobSql(lines, seed.aiRuns);
  appendAiInvocationLogSql(lines, seed.aiRuns);
  appendAiCandidateSql(lines, seed.aiRuns);
  appendAutoIncrementSql(lines, seed.idRange.nextAutoIncrement);
  return `${lines.join("\n")}\n`;
};

const appendDeleteSql = (lines, seed) => {
  const { start, end } = seed.idRange;
  lines.push(
    "DELETE FROM `ai_candidate`",
    `WHERE \`id\` BETWEEN ${start} AND ${end}`,
    `    OR \`call_id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `ai_invocation_log`",
    `WHERE \`call_id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `ai_batch_job`",
    `WHERE \`id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `classics_content_tag`",
    "WHERE `content_type` = 'WANGQI_DOCUMENT'",
    `    AND \`content_id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `classics_content_qa_pair`",
    "WHERE `content_type` = 'WANGQI_DOCUMENT'",
    `    AND \`content_id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `classics_content_version`",
    "WHERE `content_type` = 'WANGQI_DOCUMENT'",
    `    AND \`content_id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `classics_wangqi_document_event`",
    `WHERE \`document_id\` BETWEEN ${start} AND ${end};`,
    "",
    "DELETE FROM `classics_wangqi_document`",
    `WHERE \`id\` BETWEEN ${start} AND ${end};`,
    "",
  );
};

const appendDocumentSql = (lines, document) => {
  lines.push(
    "INSERT INTO `classics_wangqi_document` (",
    "    `id`, `title`, `summary`, `content_format`, `content`, `document_time`, `storage_object_id`,",
    "    `lifecycle_status`, `transition_status`, `current_publication_job_id`,",
    "    `current_version_id`, `current_version_no`, `current_versioned_at`, `content_updated_at`",
    ") VALUES",
    "    (" +
      [
        document.id,
        sqlText(document.title),
        sqlText(document.summary),
        sqlText(document.contentFormat),
        sqlText(document.content),
        document.documentTime,
        "NULL",
        sqlText(document.lifecycleStatus),
        sqlText(document.transitionStatus),
        "NULL",
        document.version.id,
        document.version.versionNo,
        epochMillis(document.versionedAt),
        epochMillis(document.contentUpdatedAt),
      ].join(", ") +
      ")",
    "ON DUPLICATE KEY UPDATE",
    "    `title` = VALUES(`title`),",
    "    `summary` = VALUES(`summary`),",
    "    `content_format` = VALUES(`content_format`),",
    "    `content` = VALUES(`content`),",
    "    `document_time` = VALUES(`document_time`),",
    "    `storage_object_id` = VALUES(`storage_object_id`),",
    "    `lifecycle_status` = VALUES(`lifecycle_status`),",
    "    `transition_status` = VALUES(`transition_status`),",
    "    `current_publication_job_id` = VALUES(`current_publication_job_id`),",
    "    `current_version_id` = VALUES(`current_version_id`),",
    "    `current_version_no` = VALUES(`current_version_no`),",
    "    `current_versioned_at` = VALUES(`current_versioned_at`),",
    "    `content_updated_at` = VALUES(`content_updated_at`);",
    "",
  );
};

const appendDocumentEventSql = (lines, document) => {
  const event = document.event;
  lines.push(
    "INSERT INTO `classics_wangqi_document_event` (",
    "    `id`, `document_id`, `title`, `occurred_at`, `occurred_label`, `summary`, `priority`",
    ") VALUES",
    "    (" +
      [
        event.id,
        document.id,
        sqlText(event.title),
        event.occurredAt,
        sqlText(event.occurredLabel),
        sqlText(event.summary),
        event.priority,
      ].join(", ") +
      ")",
    "ON DUPLICATE KEY UPDATE",
    "    `document_id` = VALUES(`document_id`),",
    "    `title` = VALUES(`title`),",
    "    `occurred_at` = VALUES(`occurred_at`),",
    "    `occurred_label` = VALUES(`occurred_label`),",
    "    `summary` = VALUES(`summary`),",
    "    `priority` = VALUES(`priority`);",
    "",
  );
};

const appendContentVersionSql = (lines, document) => {
  const version = document.version;
  lines.push(
    "INSERT INTO `classics_content_version` (",
    "    `id`, `content_type`, `content_id`, `version_no`, `versioned_at`, `snapshot_json`,",
    "    `change_type`, `change_summary`",
    ") VALUES",
    "    (" +
      [
        version.id,
        sqlText("WANGQI_DOCUMENT"),
        document.id,
        version.versionNo,
        epochMillis(document.versionedAt),
        sqlText(JSON.stringify(version.snapshot)),
        sqlText(version.changeType),
        sqlText(version.changeSummary),
      ].join(", ") +
      ")",
    "ON DUPLICATE KEY UPDATE",
    "    `version_no` = VALUES(`version_no`),",
    "    `versioned_at` = VALUES(`versioned_at`),",
    "    `snapshot_json` = VALUES(`snapshot_json`),",
    "    `change_type` = VALUES(`change_type`),",
    "    `change_summary` = VALUES(`change_summary`);",
    "",
  );
};

const appendContentTagSql = (lines, document) => {
  lines.push(
    "INSERT INTO `classics_content_tag` (",
    "    `id`, `content_type`, `content_id`, `tag_id`, `tag_name_snapshot`, `source`, `status`, `priority`",
    ") VALUES",
    document.tags
      .map((tag) =>
        row([
          tag.id,
          "WANGQI_DOCUMENT",
          document.id,
          null,
          tag.name,
          tag.source,
          tag.status,
          tag.priority,
        ]),
      )
      .join(",\n"),
    "ON DUPLICATE KEY UPDATE",
    "    `tag_id` = VALUES(`tag_id`),",
    "    `tag_name_snapshot` = VALUES(`tag_name_snapshot`),",
    "    `source` = VALUES(`source`),",
    "    `status` = VALUES(`status`),",
    "    `priority` = VALUES(`priority`);",
    "",
  );
};

const appendContentQaSql = (lines, document) => {
  lines.push(
    "INSERT INTO `classics_content_qa_pair` (",
    "    `id`, `content_type`, `content_id`, `question`, `answer`, `source`, `priority`",
    ") VALUES",
    document.qaPairs
      .map((qa) =>
        row([
          qa.id,
          "WANGQI_DOCUMENT",
          document.id,
          qa.question,
          qa.answer,
          qa.source,
          qa.priority,
        ]),
      )
      .join(",\n"),
    "ON DUPLICATE KEY UPDATE",
    "    `question` = VALUES(`question`),",
    "    `answer` = VALUES(`answer`),",
    "    `source` = VALUES(`source`),",
    "    `priority` = VALUES(`priority`);",
    "",
  );
};

const appendAiBatchJobSql = (lines, runs) => {
  lines.push(
    "INSERT INTO `ai_batch_job` (",
    "    `id`, `scope`, `capability`, `content_type`, `content_id`, `status`, `total_count`,",
    "    `success_count`, `failed_count`, `cancelled_count`, `failure_summary_json`, `requested_at`,",
    "    `cancelled_at`, `completed_at`",
    ") VALUES",
    runs
      .map((run) =>
        row([
          run.id,
          run.scope,
          run.capability,
          run.contentType,
          run.contentId,
          run.status,
          1,
          run.status === "SUCCEEDED" ? 1 : 0,
          run.status === "FAILED" ? 1 : 0,
          0,
          null,
          epochMillis(run.requestedAt),
          null,
          epochMillis(run.completedAt),
        ]),
      )
      .join(",\n"),
    "ON DUPLICATE KEY UPDATE",
    "    `scope` = VALUES(`scope`),",
    "    `capability` = VALUES(`capability`),",
    "    `content_type` = VALUES(`content_type`),",
    "    `content_id` = VALUES(`content_id`),",
    "    `status` = VALUES(`status`),",
    "    `total_count` = VALUES(`total_count`),",
    "    `success_count` = VALUES(`success_count`),",
    "    `failed_count` = VALUES(`failed_count`),",
    "    `cancelled_count` = VALUES(`cancelled_count`),",
    "    `failure_summary_json` = VALUES(`failure_summary_json`),",
    "    `requested_at` = VALUES(`requested_at`),",
    "    `cancelled_at` = VALUES(`cancelled_at`),",
    "    `completed_at` = VALUES(`completed_at`);",
    "",
  );
};

const appendAiInvocationLogSql = (lines, runs) => {
  lines.push(
    "INSERT INTO `ai_invocation_log` (",
    "    `id`, `call_id`, `batch_id`, `scope`, `capability`, `content_type`, `content_id`, `object_id`,",
    "    `service_id`, `service_role`, `model_id`, `model_name`, `prompt_version_id`, `request_id`, `trace_id`,",
    "    `status`, `stream_used`, `stream_completed`, `fallback_used`, `latency_ms`, `input_tokens`, `output_tokens`,",
    "    `cost_amount`, `failure_stage`, `result_format`, `result_payload`, `artifact_reference_json`,",
    "    `error_type`, `error_message`, `warnings_json`, `requested_at`, `completed_at`",
    ") VALUES",
    runs
      .map((run) =>
        row([
          run.id,
          run.id,
          run.id,
          run.scope,
          run.capability,
          run.contentType,
          run.contentId,
          null,
          null,
          "PRIMARY",
          run.modelId,
          run.modelName,
          run.promptVersionId,
          run.requestId,
          run.traceId,
          run.status,
          false,
          false,
          false,
          run.latencyMs,
          run.inputTokens,
          run.outputTokens,
          "0.000000",
          null,
          run.resultFormat,
          resultPayload(run),
          null,
          null,
          null,
          null,
          epochMillis(run.requestedAt),
          epochMillis(run.completedAt),
        ]),
      )
      .join(",\n"),
    "ON DUPLICATE KEY UPDATE",
    "    `batch_id` = VALUES(`batch_id`),",
    "    `scope` = VALUES(`scope`),",
    "    `capability` = VALUES(`capability`),",
    "    `content_type` = VALUES(`content_type`),",
    "    `content_id` = VALUES(`content_id`),",
    "    `status` = VALUES(`status`),",
    "    `latency_ms` = VALUES(`latency_ms`),",
    "    `input_tokens` = VALUES(`input_tokens`),",
    "    `output_tokens` = VALUES(`output_tokens`),",
    "    `result_format` = VALUES(`result_format`),",
    "    `result_payload` = VALUES(`result_payload`),",
    "    `completed_at` = VALUES(`completed_at`);",
    "",
  );
};

const appendAiCandidateSql = (lines, runs) => {
  lines.push(
    "INSERT INTO `ai_candidate` (",
    "    `id`, `call_id`, `batch_id`, `capability`, `content_type`, `content_id`, `object_id`,",
    "    `result_format`, `result_payload`, `status`, `artifact_reference_json`, `failure_stage`,",
    "    `prompt_version_id`, `model_name`, `error_type`, `error_message`, `requested_at`, `applied_at`, `rejected_at`",
    ") VALUES",
    runs
      .map((run) =>
        row([
          run.id,
          run.id,
          run.id,
          run.capability,
          run.contentType,
          run.contentId,
          null,
          run.resultFormat,
          resultPayload(run),
          "PENDING",
          null,
          null,
          run.promptVersionId,
          run.modelName,
          null,
          null,
          epochMillis(run.completedAt),
          null,
          null,
        ]),
      )
      .join(",\n"),
    "ON DUPLICATE KEY UPDATE",
    "    `call_id` = VALUES(`call_id`),",
    "    `batch_id` = VALUES(`batch_id`),",
    "    `capability` = VALUES(`capability`),",
    "    `content_type` = VALUES(`content_type`),",
    "    `content_id` = VALUES(`content_id`),",
    "    `result_format` = VALUES(`result_format`),",
    "    `result_payload` = VALUES(`result_payload`),",
    "    `status` = VALUES(`status`),",
    "    `prompt_version_id` = VALUES(`prompt_version_id`),",
    "    `model_name` = VALUES(`model_name`),",
    "    `requested_at` = VALUES(`requested_at`),",
    "    `applied_at` = VALUES(`applied_at`),",
    "    `rejected_at` = VALUES(`rejected_at`);",
    "",
  );
};

const appendAutoIncrementSql = (lines, nextAutoIncrement) => {
  for (const table of [
    "classics_wangqi_document",
    "classics_wangqi_document_event",
    "classics_content_version",
    "classics_content_tag",
    "classics_content_qa_pair",
    "ai_invocation_log",
    "ai_batch_job",
    "ai_candidate",
  ]) {
    lines.push(`ALTER TABLE \`${table}\` AUTO_INCREMENT = ${nextAutoIncrement};`);
  }
};

const resultPayload = (run) =>
  typeof run.resultPayload === "string"
    ? run.resultPayload
    : JSON.stringify(run.resultPayload);

const row = (values) => `    (${values.map(sqlValue).join(", ")})`;

const sqlValue = (value) => {
  if (value === null || value === undefined) {
    return "NULL";
  }
  if (typeof value === "number") {
    return String(value);
  }
  if (typeof value === "boolean") {
    return value ? "1" : "0";
  }
  if (isRawSql(value)) {
    return value.rawSql;
  }
  return sqlText(value);
};

const sqlText = (value) => `'${String(value).replace(/'/g, "''")}'`;

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

const assertNonBlank = (value, field) => {
  if (String(value ?? "").trim() === "") {
    throw new Error(`Missing ${field}`);
  }
};

const assertPositiveInteger = (value, field) => {
  if (!Number.isSafeInteger(value) || value <= 0) {
    throw new Error(`${field} must be a positive integer`);
  }
};

const assertInRange = (value, range, field) => {
  assertPositiveInteger(value, field);
  if (value < range.start || value > range.end) {
    throw new Error(`${field} must be between ${range.start} and ${range.end}`);
  }
};

main();
