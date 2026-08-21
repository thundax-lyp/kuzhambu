#!/usr/bin/env node

import { spawnSync } from "node:child_process";
import { createRequire } from "node:module";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { readFileSync } from "node:fs";
import { writeFile } from "node:fs/promises";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const requireFromAdmin = createRequire(resolve(repoRoot, "kuzhambu-apps/admin-web/package.json"));
const { sm2 } = requireFromAdmin("sm-crypto");
const types = ["SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS"];
const args = parseArgs(process.argv.slice(2));
for (const name of ["run-id", "evidence-file", "seed-env-file", "admin-base-url", "portal-base-url"]) {
  if (!args[name]) throw new Error(`Missing --${name}`);
}

const parameters = {
  batchSize: positiveInt(process.env.KUZHAMBU_SMOKE_BATCH_SIZE, 20),
  pollIntervalSeconds: positiveInt(process.env.KUZHAMBU_SMOKE_POLL_INTERVAL_SECONDS, 5),
  deadlineSeconds: positiveInt(process.env.KUZHAMBU_SMOKE_DEADLINE_SECONDS, 900),
};
const evidence = {
  smokeRunId: args["run-id"],
  generatedAt: Date.now(),
  mode: "fresh-full",
  parameters,
  expected: sets(),
  accepted: sets(),
  successfulJobs: sets(),
  publishedContents: sets(),
  readyDocuments: sets(),
  portalList: { SANCAI_ENTRY: [] },
  portalDetails: { SANCAI_ENTRY: [] },
  extractionTasks: sets(),
  adoptedTasks: sets(),
  publishedMaterials: sets(),
  visibleGraphs: sets(),
  failures: [],
};

try {
  const database = databaseClient(args["seed-env-file"]);
  evidence.expected = queryContentSets(database, false);
  for (const type of types) if (evidence.expected[type].length === 0) throw new Error(`${type} expected set is empty`);
  const token = await login();
  const jobs = await publishAll(token);
  await pollJobs(database, jobs);
  evidence.publishedContents = queryContentSets(database, true);
  assertExpected(evidence.publishedContents, "publishedContents");
  // CONTENT_COMMITTED is reached only after the Discovery document is READY.
  evidence.successfulJobs = cloneSets(evidence.expected);
  evidence.readyDocuments = cloneSets(evidence.expected);
  await verifySancaiPortal();
  await executeGraphFlow(token);
} catch (error) {
  evidence.failures.push({
    step: "full-smoke-api-flow",
    lastStatus: "FAILED",
    reason: safeMessage(error),
    waitedSeconds: 0,
    deadlineSeconds: parameters.deadlineSeconds,
  });
} finally {
  evidence.generatedAt = Date.now();
  await writeFile(args["evidence-file"], `${JSON.stringify(evidence, null, 2)}\n`, { flag: "wx" });
}
if (evidence.failures.length > 0) process.exitCode = 1;

async function publishAll(token) {
  const paths = {
    SANCAI_ENTRY: "/api/classics/publication/sancai/entries/batch/publish",
    WANGQI_DOCUMENT: "/api/classics/publication/wangqi/documents/batch/publish",
    MING_CUSTOMS: "/api/classics/publication/ming-customs/batch/publish",
  };
  const jobs = [];
  for (const type of types) {
    for (const ids of chunks(evidence.expected[type], parameters.batchSize)) {
      const response = await adminPost(paths[type], { ids: ids.map(Number) }, token);
      if (Number(response.rejectedCount) !== 0 || !Array.isArray(response.items)) {
        throw new Error(`${type} batch publication rejected content`);
      }
      for (const item of response.items) {
        if (!item.accepted || !item.jobId) throw new Error(`${type}:${item.contentId} was not accepted`);
        evidence.accepted[type].push(String(item.contentId));
        jobs.push(String(item.jobId));
      }
    }
  }
  assertExpected(evidence.accepted, "accepted");
  return jobs;
}

async function pollJobs(database, jobs) {
  await pollUntil("publication jobs", () => {
    const rows = database(
      `select job_result_status, job_status from classics_publication_job where id in (${jobs.map(sqlId).join(",")})`,
    );
    if (rows.some(row => row[0] === "FAILED")) throw new Error("A publication job failed");
    return rows.length === jobs.length && rows.every(row => row[0] === "SUCCEEDED" && row[1] === "CONTENT_COMMITTED");
  });
}

async function verifySancaiPortal() {
  const expected = new Set(evidence.expected.SANCAI_ENTRY);
  for (let pageNo = 1; ; pageNo += 1) {
    const page = await portalPost("/api/portal/classics/sancai/entries/page", {
      pageNo: String(pageNo),
      pageSize: "200",
    });
    const records = page.records ?? page.list ?? [];
    for (const record of records) {
      const id = String(record.id ?? record.entryId);
      if (expected.has(id)) evidence.portalList.SANCAI_ENTRY.push(id);
    }
    if (records.length < 200) break;
  }
  for (const id of evidence.expected.SANCAI_ENTRY) {
    const detail = await portalPost("/api/portal/classics/sancai/entries/get", { id: Number(id) });
    if (String(detail.id ?? detail.entryId) !== id) throw new Error(`Sancai portal detail mismatch: ${id}`);
    evidence.portalDetails.SANCAI_ENTRY.push(id);
  }
}

async function executeGraphFlow(token) {
  for (const type of types) {
    for (const id of evidence.expected[type]) await executeMaterialGraph(type, id, token);
  }
}

async function executeMaterialGraph(type, id, token) {
  const contentRef = { contentType: type, contentRefId: id };
  const created = await adminPost(
    "/api/knowledge/graph/material/extraction/create",
    { ...contentRef, idempotencyKey: `${args["run-id"]}:extract:${type}:${id}` },
    token,
  );
  const taskId = String(created.id);
  evidence.extractionTasks[type].push(id);
  let detail;
  await pollUntil(`graph task ${taskId}`, async () => {
    detail = await adminPost("/api/knowledge/graph/task/get", { taskId }, token);
    if (["FAILED", "CANCELLED"].includes(detail.task.executionStatus)) {
      throw new Error(`Graph task ${taskId} failed at ${detail.task.currentStage}: ${detail.task.failureReason ?? "unknown"}`);
    }
    return detail.task.executionStatus === "SUCCEEDED";
  });
  let task = detail.task;
  if (task.disposition === "PENDING") {
    const material = await adminPost("/api/knowledge/graph/material/get", contentRef, token);
    const applied = await adminPost(
      "/api/knowledge/graph/task/candidate/apply",
      {
        taskId,
        taskLockVersion: task.lockVersion,
        expectedExecutionStatus: "SUCCEEDED",
        expectedDisposition: "PENDING",
        materialLockVersion: material.material.lockVersion,
        applyMode: "MERGE",
        idempotencyKey: `${args["run-id"]}:apply:${type}:${id}`,
      },
      token,
    );
    task = applied.task;
  }
  if (!["ADOPTED_MERGE", "ADOPTED_REPLACE"].includes(task.disposition)) {
    throw new Error(`${type}:${id} candidate was not adopted`);
  }
  evidence.adoptedTasks[type].push(id);
  const preview = await adminPost("/api/knowledge/graph/publication/preview", contentRef, token);
  if (!preview.publishable || !preview.previewToken) throw new Error(`${type}:${id} graph is not publishable`);
  const published = await adminPost(
    "/api/knowledge/graph/publication/publish",
    {
      ...contentRef,
      materialLockVersion: preview.materialLockVersion,
      previewToken: preview.previewToken,
      conflictDecisions: [],
    },
    token,
  );
  if (!published.success || published.materialStatus !== "PUBLISHED") {
    throw new Error(`${type}:${id} graph publication failed`);
  }
  evidence.publishedMaterials[type].push(id);
  const visible = await portalPost("/api/portal/knowledge/graph/material/get", contentRef);
  if (!visible.visible || !Array.isArray(visible.nodes) || visible.nodes.length === 0) {
    throw new Error(`${type}:${id} graph is not anonymously visible`);
  }
  evidence.visibleGraphs[type].push(id);
}

async function login() {
  const preAuth = await postJson(`${args["admin-base-url"]}/api/auth/session/pre-auth-session/request`, {}, {});
  const response = await postJson(
    `${args["admin-base-url"]}/api/auth/session/login`,
    {
      loginToken: preAuth.loginToken,
      userName: process.env.KUZHAMBU_SMOKE_ADMIN_USERNAME ?? "admin",
      password: sm2.doEncrypt(process.env.KUZHAMBU_SMOKE_ADMIN_PASSWORD ?? "admin", preAuth.publicKey, 0),
      captcha: process.env.KUZHAMBU_SMOKE_ADMIN_CAPTCHA ?? "6666",
    },
    {},
  );
  if (!response.token) throw new Error("Admin login did not return an access token");
  return response.token;
}

async function adminPost(path, body, token) {
  return postJson(`${args["admin-base-url"]}${path}`, body, { "Access-Token": token });
}

async function portalPost(path, body) {
  return postJson(`${args["portal-base-url"]}${path}`, body, {});
}

async function postJson(url, body, headers) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...headers },
    body: JSON.stringify(body),
  });
  const json = await response.json();
  if (!response.ok || (json.code != null && ![0, 200].includes(Number(json.code)))) {
    throw new Error(`HTTP ${response.status} ${url}: ${json.message ?? json.msg ?? "request failed"}`);
  }
  return json.data ?? json;
}

function databaseClient(envFile) {
  const env = parseEnv(readFileSync(envFile, "utf8"));
  const jdbc = (env.KUZHAMBU_DB_URL ?? "").match(/^jdbc:mysql:\/\/([^:/?]+)(?::(\d+))?\/([^?]+)/);
  const host = jdbc?.[1] ?? env.MYSQL_HOST ?? "127.0.0.1";
  const port = jdbc?.[2] ?? env.MYSQL_PORT ?? "3306";
  const database = jdbc?.[3] ?? env.MYSQL_DATABASE ?? "kuzhambu";
  const user = env.KUZHAMBU_DB_USERNAME ?? env.MYSQL_USERNAME ?? "kuzhambu";
  const password = env.KUZHAMBU_DB_PASSWORD ?? env.MYSQL_PASSWORD ?? "kuzhambu";
  return sql => {
    const result = spawnSync(
      "mysql",
      ["--batch", "--skip-column-names", "-h", host, "-P", port, "-u", user, database, "-e", sql],
      { encoding: "utf8", env: { ...process.env, MYSQL_PWD: password } },
    );
    if (result.status !== 0) throw new Error(`Database verification failed: ${result.stderr.trim()}`);
    return result.stdout.trim() ? result.stdout.trim().split("\n").map(line => line.split("\t")) : [];
  };
}

function queryContentSets(database, publishedOnly) {
  const where = publishedOnly ? " where lifecycle_status='PUBLISHED'" : "";
  const rows = database(
    `select 'SANCAI_ENTRY', id from classics_sancai_entry${where} union all ` +
      `select 'WANGQI_DOCUMENT', id from classics_wangqi_document${where} union all ` +
      `select 'MING_CUSTOMS', id from classics_ming_customs_entry${where} order by 1, 2`,
  );
  const result = sets();
  for (const [type, id] of rows) result[type].push(String(id));
  return result;
}

async function pollUntil(name, probe) {
  const deadline = Date.now() + parameters.deadlineSeconds * 1000;
  while (Date.now() < deadline) {
    if (await probe()) return;
    await new Promise(done => setTimeout(done, parameters.pollIntervalSeconds * 1000));
  }
  throw new Error(`${name} exceeded ${parameters.deadlineSeconds}s deadline`);
}

function assertExpected(actual, name) {
  for (const type of types) {
    if (JSON.stringify([...actual[type]].sort()) !== JSON.stringify([...evidence.expected[type]].sort())) {
      throw new Error(`${name}.${type} does not match the imported manifest`);
    }
  }
}

function sets() {
  return Object.fromEntries(types.map(type => [type, []]));
}

function cloneSets(value) {
  return Object.fromEntries(types.map(type => [type, [...value[type]]]));
}

function chunks(values, size) {
  const result = [];
  for (let index = 0; index < values.length; index += size) result.push(values.slice(index, index + size));
  return result;
}

function parseArgs(values) {
  const result = {};
  for (let index = 0; index < values.length; index += 2) result[values[index].replace(/^--/, "")] = values[index + 1];
  return result;
}

function parseEnv(source) {
  const result = {};
  for (const line of source.split(/\r?\n/)) {
    const match = line.match(/^([A-Za-z_][A-Za-z0-9_]*)=(.*)$/);
    if (match) result[match[1]] = match[2].replace(/^['"]|['"]$/g, "");
  }
  return result;
}

function positiveInt(value, fallback) {
  const result = Number(value ?? fallback);
  if (!Number.isInteger(result) || result <= 0) throw new Error(`Expected a positive integer, received ${value}`);
  return result;
}

function sqlId(value) {
  if (!/^\d+$/.test(value)) throw new Error("Unexpected non-numeric database identifier");
  return value;
}

function safeMessage(error) {
  return String(error?.message ?? error).replace(/(token|password|api[-_ ]?key)=[^\s,]+/gi, "$1=[REDACTED]");
}
