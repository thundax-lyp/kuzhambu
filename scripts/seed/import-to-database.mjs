#!/usr/bin/env node

import mysql from "mysql2/promise";
import { readFile } from "node:fs/promises";
import { existsSync } from "node:fs";
import { basename, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const DEFAULT_BATCH_SIZE = 100;
const PROJECT_TABLE_PREFIXES = [
  "system_",
  "storage_",
  "classics_",
  "ai_",
  "knowledge_",
  "discovery_",
  "operations_",
];
const SYSTEM_CATALOGS = new Set([
  "information_schema",
  "mysql",
  "performance_schema",
  "sys",
]);
const TARGET_TABLE_PATTERN =
  /^\s*(?:INSERT(?:\s+IGNORE)?\s+INTO|REPLACE\s+INTO|DELETE\s+FROM|ALTER\s+TABLE|UPDATE)\s+`?([A-Za-z0-9_]+)`?/i;

const SCHEMA_FILES = [
  "system.sql",
  "storage.sql",
  "classics.sql",
  "ai.sql",
  "knowledge.sql",
  "discovery.sql",
  "operations.sql",
].map((name) => resolve(repoRoot, "db/schema", name));

const SEED_SQL_DIR = resolve(repoRoot, "build/seed-sql");

const DATA_FILES = [
  "system.sql",
  "storage.sql",
  "classics.sql",
  "ai.sql",
  "knowledge.sql",
  "discovery.sql",
  "operations.sql",
].map((name) => resolve(SEED_SQL_DIR, name));

const AI_LEGACY_CAPABILITY_CODES = [
  ["classics_translate", "CLASSICS_TRANSLATE"],
  ["classics_translate_batch_item", "CLASSICS_TRANSLATE_BATCH_ITEM"],
  ["classics_summary", "CLASSICS_SUMMARY"],
  ["classics_tags", "CLASSICS_TAG_EXTRACT"],
  ["classics_qa", "CLASSICS_QA"],
  ["classics_split", "CLASSICS_SPLIT"],
  ["classics_image_describe", "CLASSICS_IMAGE_DESCRIBE"],
  ["classics_image_prompt_fusion", "CLASSICS_IMAGE_PROMPT_FUSION"],
  ["classics_visual_describe", "CLASSICS_VISUAL_DESCRIBE"],
  ["classics_image_generate", "CLASSICS_IMAGE_GENERATE"],
  ["discovery_query_understanding", "DISCOVERY_QUERY_UNDERSTANDING"],
  ["discovery_answer_generation", "DISCOVERY_ANSWER_GENERATION"],
  ["knowledge_graph_extract", "KNOWLEDGE_GRAPH_EXTRACT"],
  ["knowledge_relation_extract", "KNOWLEDGE_RELATION_EXTRACT"],
  ["knowledge_lineage_extract", "KNOWLEDGE_LINEAGE_EXTRACT"],
  ["knowledge_tags", "KNOWLEDGE_TAG_EXTRACT"],
  ["platform_version_summary", "PLATFORM_VERSION_SUMMARY"],
  ["prompt_suggestion", "PROMPT_SUGGEST"],
];

const AI_RUNTIME_CAPABILITY_TABLES = [
  "ai_invocation_log",
  "ai_candidate",
  "ai_batch_job",
];

const main = async () => {
  const options = parseArgs(process.argv.slice(2));
  if (!existsSync(options.envFile)) {
    throw new Error(`env file not found: ${options.envFile}`);
  }
  const env = await readDotenv(options.envFile);
  const connectionOptions = parseMysqlOptions(env);

  if (options.rebuild) {
    await withConnection(connectionOptions, async (connection) => {
      await rebuildProjectTables(connection, options.schemaFiles);
    });
  }

  const phases = await planSeedTasks(options.dataFiles);
  const progress = new Progress(
    phases.reduce((sum, phase) => sum + phase.length, 0),
    phases.flat().reduce((sum, task) => sum + task.batchCount, 0),
  );
  let phaseIndex = 1;
  for (const phase of phases) {
    if (phase.length > 0) {
      progress.phase(phaseIndex, phase.length);
      await runTaskPhase(connectionOptions, phase, options.jobs, progress);
    }
    phaseIndex += 1;
  }

  await withConnection(connectionOptions, async (connection) => {
    await upsertAiRuntimeModels(connection, env);
    await normalizeAiRuntimeCapabilityCodes(connection);
    await verifySeed(connection);
  });

  console.log(
    `Imported ${options.schemaFiles.length} schema files and ${options.dataFiles.length} seed files${
      options.rebuild ? " after project-table rebuild" : ""
    }.`,
  );
};

const parseArgs = (args) => {
  const options = {
    envFile: resolve(repoRoot, "dev.env"),
    rebuild: false,
    includeTest: false,
    jobs: 4,
    schemaFiles: [...SCHEMA_FILES],
    dataFiles: [...DATA_FILES],
  };
  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index];
    switch (arg) {
      case "--":
        break;
      case "--env":
        options.envFile = resolvePathValue(args, ++index, arg);
        break;
      case "--rebuild":
        options.rebuild = true;
        break;
      case "--include-test":
        options.includeTest = true;
        break;
      case "--jobs":
        options.jobs = Number(resolveRawValue(args, ++index, arg));
        break;
      case "--schema":
        options.schemaFiles.push(resolvePathValue(args, ++index, arg));
        break;
      case "--data":
        options.dataFiles.push(resolvePathValue(args, ++index, arg));
        break;
      case "-h":
      case "--help":
        printUsage();
        process.exit(0);
        break;
      default:
        throw new Error(`unexpected argument: ${arg}`);
    }
  }
  if (!Number.isInteger(options.jobs) || options.jobs < 1 || options.jobs > 16) {
    throw new Error("--jobs must be an integer between 1 and 16");
  }
  if (options.includeTest) {
    options.dataFiles.push(resolve(SEED_SQL_DIR, "test.sql"));
  }
  return options;
};

const resolveRawValue = (args, index, flag) => {
  if (index >= args.length) {
    throw new Error(`${flag} requires a value`);
  }
  return args[index];
};

const resolvePathValue = (args, index, flag) => resolve(repoRoot, resolveRawValue(args, index, flag));

const printUsage = () => {
  console.log(`Usage:
  scripts/import-seed-data.sh [options]

Options:
  --env <file>       Env file. Defaults to repo-root dev.env.
  --rebuild          Drop and recreate project tables before importing.
  --include-test     Import generated test seed SQL after base seed data.
  --jobs <n>         Parallel table import workers. Defaults to 4.
  -h, --help         Show help.`);
};

const readDotenv = async (file) => {
  const values = { ...process.env };
  const content = await readFile(file, "utf8");
  for (const rawLine of content.split(/\r?\n/)) {
    let line = rawLine.trim();
    if (!line || line.startsWith("#") || !line.includes("=")) {
      continue;
    }
    if (line.startsWith("export ")) {
      line = line.slice("export ".length).trim();
    }
    const separator = line.indexOf("=");
    const key = line.slice(0, separator).trim();
    const value = stripQuotes(line.slice(separator + 1).trim());
    if (key) {
      values[key] = value;
    }
  }
  return values;
};

const stripQuotes = (value) => {
  if (value.length >= 2) {
    const first = value[0];
    const last = value[value.length - 1];
    if ((first === '"' && last === '"') || (first === "'" && last === "'")) {
      return value.slice(1, -1);
    }
  }
  return value;
};

const parseMysqlOptions = (env) => {
  const url = required(env, "KUZHAMBU_DB_URL");
  const parsed = parseJdbcMysqlUrl(url);
  return {
    host: parsed.host,
    port: parsed.port,
    user: required(env, "KUZHAMBU_DB_USERNAME"),
    password: env.KUZHAMBU_DB_PASSWORD ?? "",
    database: parsed.database,
    charset: "utf8mb4",
    multipleStatements: false,
    supportBigNumbers: true,
    bigNumberStrings: true,
    namedPlaceholders: false,
  };
};

const parseJdbcMysqlUrl = (jdbcUrl) => {
  const normalized = jdbcUrl.replace(/^jdbc:mysql:\/\//, "mysql://");
  const parsed = new URL(normalized);
  const database = parsed.pathname.replace(/^\//, "");
  if (!database) {
    throw new Error("KUZHAMBU_DB_URL must include a database name");
  }
  return {
    host: parsed.hostname,
    port: parsed.port ? Number(parsed.port) : 3306,
    database,
  };
};

const required = (env, name) => {
  const value = env[name];
  if (!value) {
    throw new Error(`missing required env value: ${name}`);
  }
  return value;
};

const withConnection = async (options, callback) => {
  const connection = await mysql.createConnection(options);
  try {
    await connection.query("SET NAMES utf8mb4");
    return await callback(connection);
  } finally {
    await connection.end();
  }
};

const rebuildProjectTables = async (connection, schemaFiles) => {
  const [[catalogRow]] = await connection.query("SELECT DATABASE() AS catalog");
  const catalog = catalogRow.catalog;
  if (!catalog || SYSTEM_CATALOGS.has(String(catalog).toLowerCase())) {
    throw new Error(`refusing to rebuild unsafe catalog: ${catalog}`);
  }
  const [tableRows] = await connection.query("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'");
  const tables = tableRows
    .map((row) => Object.values(row)[0])
    .filter((table) => PROJECT_TABLE_PREFIXES.some((prefix) => table.startsWith(prefix)))
    .sort();
  await connection.query("SET FOREIGN_KEY_CHECKS=0");
  for (const table of tables) {
    await connection.query(`DROP TABLE IF EXISTS \`${table.replaceAll("`", "``")}\``);
  }
  await connection.query("SET FOREIGN_KEY_CHECKS=1");
  for (const schemaFile of schemaFiles) {
    const statements = splitSql(await readFile(schemaFile, "utf8"));
    await executeStatements(connection, statements);
    console.log(`Imported schema file: ${schemaFile} (${statements.length} statements)`);
  }
};

const executeStatements = async (connection, statements) => {
  for (const statement of statements) {
    const sql = statement.trim();
    if (sql) {
      await connection.query(sql);
    }
  }
};

const planSeedTasks = async (seedFiles) => {
  const base = [];
  const knowledge = [];
  const test = [];
  for (const seedFile of seedFiles) {
    const tasks = await planSeedFile(seedFile);
    const fileName = basename(seedFile);
    if (fileName === "knowledge.sql") {
      knowledge.push(...tasks);
    } else if (fileName === "test.sql") {
      test.push(...tasks);
    } else {
      base.push(...tasks);
    }
  }
  const knowledgeNodes = [];
  const knowledgeEdgesAndNodeMappings = [];
  const knowledgeEdgeMappings = [];
  for (const task of knowledge) {
    if (task.table === "knowledge_graph_published_edge_material") {
      knowledgeEdgeMappings.push(task);
    } else if (
      task.table === "knowledge_graph_published_edge" ||
      task.table === "knowledge_graph_published_node_material"
    ) {
      knowledgeEdgesAndNodeMappings.push(task);
    } else {
      knowledgeNodes.push(task);
    }
  }
  return [base, knowledgeNodes, knowledgeEdgesAndNodeMappings, knowledgeEdgeMappings, test];
};

const planSeedFile = async (seedFile) => {
  const statementsByTable = new Map();
  const statements = splitSql(await readFile(seedFile, "utf8"));
  for (const statement of statements) {
    const table = targetTable(statement);
    if (!table) {
      continue;
    }
    if (!statementsByTable.has(table)) {
      statementsByTable.set(table, []);
    }
    statementsByTable.get(table).push(statement);
  }
  return [...statementsByTable.entries()].map(([table, tableStatements]) => ({
    sourceFile: seedFile,
    table,
    statements: tableStatements,
    batchCount: Math.max(1, Math.ceil(tableStatements.length / DEFAULT_BATCH_SIZE)),
    label: `${basename(seedFile)}:${table}`,
  }));
};

const targetTable = (sql) => {
  const normalized = stripLeadingComments(sql);
  if (!normalized || /^SET\s+NAMES/i.test(normalized)) {
    return null;
  }
  const match = normalized.match(TARGET_TABLE_PATTERN);
  if (!match) {
    throw new Error(`unsupported seed SQL statement: ${preview(normalized)}`);
  }
  return match[1];
};

const stripLeadingComments = (sql) => {
  let normalized = sql.trimStart();
  let changed = true;
  while (changed) {
    changed = false;
    if (normalized.startsWith("--") || normalized.startsWith("#")) {
      const lineEnd = normalized.indexOf("\n");
      normalized = lineEnd >= 0 ? normalized.slice(lineEnd + 1).trimStart() : "";
      changed = true;
    } else if (normalized.startsWith("/*")) {
      const commentEnd = normalized.indexOf("*/");
      normalized = commentEnd >= 0 ? normalized.slice(commentEnd + 2).trimStart() : "";
      changed = true;
    }
  }
  return normalized;
};

const preview = (value) => {
  const compact = value.replace(/\s+/g, " ").trim();
  return compact.length <= 160 ? compact : `${compact.slice(0, 160)}...`;
};

const runTaskPhase = async (connectionOptions, tasks, jobs, progress) => {
  let nextIndex = 0;
  const workers = Array.from({ length: Math.min(jobs, tasks.length) }, async () => {
    while (nextIndex < tasks.length) {
      const task = tasks[nextIndex];
      nextIndex += 1;
      await runTableTask(connectionOptions, task, progress);
    }
  });
  await Promise.all(workers);
};

const runTableTask = async (connectionOptions, task, progress) => {
  await withConnection(connectionOptions, async (connection) => {
    let batchIndex = 1;
    for (let offset = 0; offset < task.statements.length; offset += DEFAULT_BATCH_SIZE) {
      const batch = task.statements.slice(offset, offset + DEFAULT_BATCH_SIZE);
      progress.batchStarted(task, batchIndex);
      await connection.beginTransaction();
      try {
        await executeStatements(connection, batch);
        await connection.commit();
        progress.batchCompleted(task, batchIndex);
      } catch (error) {
        await connection.rollback();
        progress.batchFailed(task, batchIndex, error);
        throw error;
      }
      batchIndex += 1;
    }
    progress.taskCompleted(task);
  });
};

const upsertAiRuntimeModels = async (connection, env) => {
  const models = [
    aiRuntimeModel(env, "KUZHAMBU_AI_PRIMARY", "2"),
    aiRuntimeModel(env, "KUZHAMBU_AI_VISION", "1", "KUZHAMBU_AI_PRIMARY"),
    aiRuntimeModel(env, "KUZHAMBU_AI_TEXT2IMAGE", "3"),
    aiRuntimeModel(env, "KUZHAMBU_AI_BACKUP", "", "KUZHAMBU_AI_PRIMARY"),
  ].filter(Boolean);
  if (models.length === 0) {
    return;
  }
  await connection.beginTransaction();
  try {
    for (const model of models) {
      await connection.execute(
        `UPDATE ai_model
         SET api_source = COALESCE(NULLIF(?, ''), api_source),
             base_url = COALESCE(NULLIF(?, ''), base_url),
             encrypted_api_key = COALESCE(NULLIF(?, ''), encrypted_api_key),
             enabled = ?
         WHERE id = ?`,
        [model.apiSource, model.baseUrl, model.apiKey, model.enabled, model.modelId],
      );
    }
    await connection.commit();
  } catch (error) {
    await connection.rollback();
    throw error;
  }
  console.log(`Applied ${models.length} AI runtime model env overrides.`);
};

const normalizeAiRuntimeCapabilityCodes = async (connection) => {
  await connection.beginTransaction();
  try {
    let updatedRows = 0;
    for (const table of AI_RUNTIME_CAPABILITY_TABLES) {
      for (const [legacyCode, capability] of AI_LEGACY_CAPABILITY_CODES) {
        const [result] = await connection.execute(
          `UPDATE \`${table}\` SET capability = ? WHERE capability = ?`,
          [capability, legacyCode],
        );
        updatedRows += Number(result.affectedRows ?? 0);
      }
    }
    await connection.commit();
    if (updatedRows > 0) {
      console.log(`Normalized ${updatedRows} AI runtime capability values.`);
    }
  } catch (error) {
    await connection.rollback();
    throw error;
  }
};

const aiRuntimeModel = (env, prefix, defaultModelId, fallbackPrefix = null) => {
  const modelId = env[`${prefix}_MODEL_ID`] || defaultModelId;
  if (!modelId) {
    return null;
  }
  const fallback = (name) => (fallbackPrefix ? env[`${fallbackPrefix}_${name}`] : "");
  return {
    modelId: Number(modelId),
    apiSource: env[`${prefix}_API_SOURCE`] || fallback("API_SOURCE") || "",
    baseUrl: env[`${prefix}_BASE_URL`] || fallback("BASE_URL") || "",
    apiKey: env[`${prefix}_API_KEY`] || fallback("API_KEY") || "",
    enabled: Number(env[`${prefix}_ENABLED`] ?? "1"),
  };
};

const verifySeed = async (connection) => {
  const [[row]] = await connection.query(
    `SELECT
       (SELECT COUNT(*) FROM ai_model WHERE enabled = 1 AND base_url <> '' AND encrypted_api_key IS NOT NULL) AS ready_models,
       (SELECT COUNT(*) FROM ai_business_config WHERE enabled = 1) AS ai_configs,
       (SELECT api_source FROM ai_model WHERE id = 2) AS primary_ai_api_source,
       (SELECT model_id FROM ai_business_config WHERE capability = 'KNOWLEDGE_GRAPH_EXTRACT' AND enabled = 1) AS graph_extraction_model_id,
       (SELECT COUNT(*) FROM classics_sancai_entry) AS sancai_entries,
       (SELECT COUNT(*) FROM classics_wangqi_document) AS wangqi_documents,
       (SELECT COUNT(*) FROM classics_ming_customs_entry) AS ming_entries`,
  );
  if (Number(row.ready_models) < 1) {
    throw new Error("no AI model has runtime baseUrl/apiKey from env");
  }
  if (Number(row.ai_configs) < 1) {
    throw new Error("AI business configs were not imported");
  }
  if (row.primary_ai_api_source !== "OPENAI_COMPATIBLE") {
    throw new Error(
      "AI primary model 2 must use OPENAI_COMPATIBLE; check KUZHAMBU_AI_PRIMARY_API_SOURCE",
    );
  }
  if (Number(row.graph_extraction_model_id) !== 2) {
    throw new Error(
      "KNOWLEDGE_GRAPH_EXTRACT must use reproducible primary model 2 seed configuration",
    );
  }
  if (
    Number(row.sancai_entries) < 1 ||
    Number(row.wangqi_documents) < 1 ||
    Number(row.ming_entries) < 1
  ) {
    throw new Error("Classics seed data is incomplete");
  }
};

const splitSql = (sql) => {
  const statements = [];
  let current = "";
  let singleQuoted = false;
  let doubleQuoted = false;
  let backticked = false;
  let lineComment = false;
  let blockComment = false;

  for (let index = 0; index < sql.length; index += 1) {
    const char = sql[index];
    const nextChar = sql[index + 1] ?? "";
    if (lineComment) {
      current += char;
      if (char === "\n") {
        lineComment = false;
      }
      continue;
    }
    if (blockComment) {
      current += char;
      if (char === "*" && nextChar === "/") {
        current += nextChar;
        index += 1;
        blockComment = false;
      }
      continue;
    }
    if (!singleQuoted && !doubleQuoted && !backticked) {
      if (char === "-" && nextChar === "-") {
        lineComment = true;
        current += char + nextChar;
        index += 1;
        continue;
      }
      if (char === "#") {
        lineComment = true;
        current += char;
        continue;
      }
      if (char === "/" && nextChar === "*") {
        blockComment = true;
        current += char + nextChar;
        index += 1;
        continue;
      }
    }
    if (!doubleQuoted && !backticked && char === "'" && !isEscaped(sql, index)) {
      singleQuoted = !singleQuoted;
    } else if (!singleQuoted && !backticked && char === '"' && !isEscaped(sql, index)) {
      doubleQuoted = !doubleQuoted;
    } else if (!singleQuoted && !doubleQuoted && char === "`") {
      backticked = !backticked;
    }
    if (!singleQuoted && !doubleQuoted && !backticked && char === ";") {
      const statement = current.trim();
      if (statement) {
        statements.push(statement);
      }
      current = "";
    } else {
      current += char;
    }
  }
  const tail = current.trim();
  if (tail) {
    statements.push(tail);
  }
  return statements;
};

const isEscaped = (sql, index) => {
  let slashCount = 0;
  for (let cursor = index - 1; cursor >= 0 && sql[cursor] === "\\"; cursor -= 1) {
    slashCount += 1;
  }
  return slashCount % 2 === 1;
};

class Progress {
  #completedTasks = 0;
  #completedBatches = 0;

  constructor(totalTasks, totalBatches) {
    this.totalTasks = totalTasks;
    this.totalBatches = totalBatches;
  }

  phase(phaseIndex, taskCount) {
    console.log(`Seed reactor phase ${phaseIndex}: ${taskCount} table tasks`);
  }

  batchStarted(task, batchIndex) {
    console.log(`${this.#bar()} RUN ${task.label} batch ${batchIndex}/${task.batchCount}`);
  }

  batchCompleted(task, batchIndex) {
    this.#completedBatches += 1;
    console.log(`${this.#bar()} DONE ${task.label} batch ${batchIndex}/${task.batchCount}`);
  }

  batchFailed(task, batchIndex, error) {
    console.log(`${this.#bar()} FAIL ${task.label} batch ${batchIndex}/${task.batchCount}: ${error.message}`);
  }

  taskCompleted(task) {
    this.#completedTasks += 1;
    console.log(
      `${this.#bar()} TABLE ${task.label} complete (${this.#completedTasks}/${this.totalTasks} tasks)`,
    );
  }

  #bar() {
    const width = 24;
    const filled =
      this.totalBatches === 0
        ? width
        : Math.min(width, Math.floor((this.#completedBatches * width) / this.totalBatches));
    return `[${"#".repeat(filled)}${"-".repeat(width - filled)}] ${this.#completedBatches}/${this.totalBatches}`;
  }
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
