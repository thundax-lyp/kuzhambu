import { readdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

type JsonObject = Record<string, unknown>;

type AiPromptMeta = {
  templateId: number;
  promptVersionId: number;
  variableIdStart: number;
  capability: string;
  name: string;
  description: string;
  status?: string;
  versionNo: number;
  current?: boolean;
  suite: string;
  outputSchema: JsonObject;
  changeSummary: string;
  registeredAt?: string;
  variables: Array<{
    name: string;
    required: boolean;
    description: string;
    priority: number;
  }>;
};

type PromptSeed = AiPromptMeta & {
  systemTemplate: string;
  userTemplate: string;
};

const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "..");
const sourceRoot = resolve(repoRoot, "db/data-source/ai-prompts");
const outputPath = resolve(repoRoot, "db/data/ai.sql");
const REGISTERED_AT = "2026-02-27 04:00:00.000";

const main = () => {
  const prompts = readPromptSeeds(sourceRoot);
  const sql = generate(prompts);

  if (process.argv.includes("--check")) {
    const current = readFileSync(outputPath, "utf8");
    if (current !== sql) {
      console.error(
        "db/data/ai.sql is out of date. Run: node scripts/generate-ai-data-sql.ts",
      );
      process.exit(1);
    }
    return;
  }

  writeFileSync(outputPath, sql);
};

const readPromptSeeds = (root: string) => {
  const promptDirs: string[] = [];
  for (const scope of sortedDirs(root)) {
    for (const prompt of sortedDirs(scope)) {
      promptDirs.push(prompt);
    }
  }

  const prompts = promptDirs.map((promptDir) => {
    const meta = JSON.parse(
      readFileSync(join(promptDir, "meta.json"), "utf8"),
    ) as AiPromptMeta;
    return {
      ...meta,
      status: meta.status ?? "ACTIVE",
      registeredAt: meta.registeredAt ?? REGISTERED_AT,
      systemTemplate: readFileSync(
        join(promptDir, "system-template.txt"),
        "utf8",
      ).trim(),
      userTemplate: readFileSync(
        join(promptDir, "user-template.txt"),
        "utf8",
      ).trim(),
    };
  });

  validatePromptSeeds(prompts);
  return prompts.sort((a, b) => a.templateId - b.templateId);
};

const sortedDirs = (path: string) =>
  readdirSync(path)
    .map((name) => join(path, name))
    .filter((child) => statSync(child).isDirectory())
    .sort((a, b) => a.localeCompare(b));

const validatePromptSeeds = (prompts: PromptSeed[]) => {
  assertUnique(
    prompts.map((prompt) => prompt.templateId),
    "templateId",
  );
  assertUnique(
    prompts.map((prompt) => prompt.promptVersionId),
    "promptVersionId",
  );
  assertUnique(
    prompts.flatMap((prompt) =>
      prompt.variables.map((_, index) => prompt.variableIdStart + index),
    ),
    "variableId",
  );
  assertUnique(
    prompts.map((prompt) => prompt.capability),
    "capability",
  );
  for (const prompt of prompts) {
    assertPositiveInteger(prompt.templateId, "templateId");
    assertPositiveInteger(prompt.promptVersionId, "promptVersionId");
    assertPositiveInteger(prompt.variableIdStart, "variableIdStart");
    assertPositiveInteger(prompt.versionNo, "versionNo");
    assertNonBlank(prompt.capability, "capability");
    assertNonBlank(prompt.name, "name");
    assertNonBlank(prompt.systemTemplate, "system-template.txt");
    assertNonBlank(prompt.userTemplate, "user-template.txt");
  }
};

const generate = (prompts: PromptSeed[]) => {
  const lines: string[] = ["SET NAMES utf8mb4;", ""];
  appendModelSql(lines);
  appendCapabilityMappingSql(lines);
  appendActionStatusSql(lines);
  appendPromptTemplateSql(lines, prompts);
  appendPromptVersionSql(lines, prompts);
  appendPromptVariableSql(lines, prompts);
  return lines.join("\n");
};

const appendModelSql = (lines: string[]) => {
  lines.push("INSERT INTO `ai_model` (");
  lines.push(
    "    `id`, `api_source`, `base_url`, `encrypted_api_key`, `model_name`, `display_name`, `capabilities_json`,",
  );
  lines.push(
    "    `default_params_json`, `description`, `enabled`, `registered_at`",
  );
  lines.push(") VALUES");
  lines.push(
    [
      [
        900101,
        "OPENAI",
        "",
        null,
        "CTYUN-CX-Qwen3.5-397B-A17B",
        "CTYUN Qwen3.5 397B",
        JSON.stringify(["TEXT2TEXT", "IMAGE2TEXT"]),
        JSON.stringify({ temperature: 0.2, max_tokens: 4096 }),
        "Default OpenAI-compatible vision-capable model for classics AI.",
        1,
        REGISTERED_AT,
      ],
      [
        900102,
        "OPENAI",
        "",
        null,
        "CTYUN-bot-DeepSeek-V3.2-pro",
        "CTYUN DeepSeek V3.2 Pro",
        JSON.stringify(["TEXT2TEXT"]),
        JSON.stringify({ temperature: 0.2, max_tokens: 4096 }),
        "Default OpenAI-compatible LLM from local server configuration.",
        1,
        REGISTERED_AT,
      ],
      [
        900201,
        "BYTEDANCE",
        "",
        null,
        "doubao-seedream-5-0-pro-260628",
        "Doubao Seedream 5.0 Pro",
        JSON.stringify(["TEXT2IMAGE"]),
        JSON.stringify({
          response_format: "url",
          size: "2K",
          stream: false,
          watermark: true,
        }),
        "Default ByteDance text-to-image model.",
        1,
        REGISTERED_AT,
      ],
    ]
      .map((values) => row(values))
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `api_source` = VALUES(`api_source`),");
  lines.push("    `base_url` = VALUES(`base_url`),");
  lines.push("    `encrypted_api_key` = VALUES(`encrypted_api_key`),");
  lines.push("    `display_name` = VALUES(`display_name`),");
  lines.push("    `capabilities_json` = VALUES(`capabilities_json`),");
  lines.push("    `default_params_json` = VALUES(`default_params_json`),");
  lines.push("    `description` = VALUES(`description`),");
  lines.push("    `enabled` = VALUES(`enabled`),");
  lines.push("    `registered_at` = VALUES(`registered_at`);");
  lines.push("");
};

const appendCapabilityMappingSql = (lines: string[]) => {
  lines.push("INSERT INTO `ai_capability_mapping` (");
  lines.push(
    "    `mapping_id`, `scope`, `capability`, `model_id`, `enabled`, `configured_at`",
  );
  lines.push(") VALUES");
  lines.push(
    [
      [910101, "classics", "classics_summary", 900102, 1, REGISTERED_AT],
      [910102, "classics", "classics_tags", 900102, 1, REGISTERED_AT],
      [910103, "classics", "classics_qa", 900102, 1, REGISTERED_AT],
      [910104, "classics", "classics_image_describe", 900101, 1, REGISTERED_AT],
      [910105, "classics", "classics_translate", 900102, 1, REGISTERED_AT],
      [910106, "classics", "classics_image_generate", 900201, 1, REGISTERED_AT],
      [910107, "classics", "classics_image_prompt_fusion", 900102, 1, REGISTERED_AT],
      [910108, "classics", "classics_visual_describe", 900102, 1, REGISTERED_AT],
      [910201, "discovery", "discovery_query_understanding", 900102, 1, REGISTERED_AT],
      [910202, "discovery", "discovery_answer_generation", 900102, 1, REGISTERED_AT],
    ]
      .map((values) => row(values))
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `model_id` = VALUES(`model_id`),");
  lines.push("    `enabled` = VALUES(`enabled`),");
  lines.push("    `configured_at` = VALUES(`configured_at`);");
  lines.push("");
};

const appendActionStatusSql = (lines: string[]) => {
  lines.push("INSERT INTO `ai_action_status` (");
  lines.push(
    "    `action_status_id`, `scope`, `capability`, `available`, `unavailable_reason`, `checked_at`",
  );
  lines.push(") VALUES");
  lines.push(
    [
      [920101, "classics", "classics_summary", 1, null, REGISTERED_AT],
      [920102, "classics", "classics_tags", 1, null, REGISTERED_AT],
      [920103, "classics", "classics_qa", 1, null, REGISTERED_AT],
      [920104, "classics", "classics_image_describe", 1, null, REGISTERED_AT],
      [920105, "classics", "classics_translate", 1, null, REGISTERED_AT],
      [920106, "classics", "classics_image_generate", 1, null, REGISTERED_AT],
      [920107, "classics", "classics_image_prompt_fusion", 1, null, REGISTERED_AT],
      [920108, "classics", "classics_visual_describe", 1, null, REGISTERED_AT],
      [920201, "discovery", "discovery_query_understanding", 1, null, REGISTERED_AT],
      [920202, "discovery", "discovery_answer_generation", 1, null, REGISTERED_AT],
    ]
      .map((values) => row(values))
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `available` = VALUES(`available`),");
  lines.push("    `unavailable_reason` = VALUES(`unavailable_reason`),");
  lines.push("    `checked_at` = VALUES(`checked_at`);");
  lines.push("");
};

const appendPromptTemplateSql = (lines: string[], prompts: PromptSeed[]) => {
  lines.push("INSERT INTO `ai_prompt_template` (");
  lines.push(
    "    `id`, `capability`, `name`, `description`, `status`,",
  );
  lines.push("    `current_version_no`, `registered_at`");
  lines.push(") VALUES");
  lines.push(
    prompts
      .map((prompt) =>
        row([
          prompt.templateId,
          prompt.capability,
          prompt.name,
          prompt.description,
          prompt.status,
          prompt.current ? prompt.versionNo : null,
          prompt.registeredAt,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `capability` = VALUES(`capability`),");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `description` = VALUES(`description`),");
  lines.push("    `status` = VALUES(`status`),");
  lines.push("    `current_version_no` = VALUES(`current_version_no`),");
  lines.push("    `registered_at` = VALUES(`registered_at`);");
  lines.push("");
};

const appendPromptVersionSql = (lines: string[], prompts: PromptSeed[]) => {
  lines.push("INSERT INTO `ai_prompt_version` (");
  lines.push(
    "    `id`, `template_id`, `version_no`, `message_templates_json`,",
  );
  lines.push(
    "    `variables_snapshot_json`, `output_schema_json`, `change_summary`, `registered_at`",
  );
  lines.push(") VALUES");
  lines.push(
    prompts
      .map((prompt) =>
        row([
          prompt.promptVersionId,
          prompt.templateId,
          prompt.versionNo,
          JSON.stringify([
            { role: "system", content: prompt.systemTemplate },
            { role: "user", content: prompt.userTemplate },
          ]),
          JSON.stringify(
            prompt.variables.map(({ name, required, description }) => ({
              name,
              required,
              description,
            })),
          ),
          JSON.stringify(prompt.outputSchema),
          prompt.changeSummary,
          prompt.registeredAt,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push(
    "    `message_templates_json` = VALUES(`message_templates_json`),",
  );
  lines.push(
    "    `variables_snapshot_json` = VALUES(`variables_snapshot_json`),",
  );
  lines.push("    `output_schema_json` = VALUES(`output_schema_json`),");
  lines.push("    `change_summary` = VALUES(`change_summary`),");
  lines.push("    `registered_at` = VALUES(`registered_at`);");
  lines.push("");
};

const appendPromptVariableSql = (lines: string[], prompts: PromptSeed[]) => {
  lines.push("INSERT INTO `ai_prompt_variable` (");
  lines.push(
    "    `id`, `template_id`, `variable_name`, `required`, `description`, `priority`",
  );
  lines.push(") VALUES");
  lines.push(
    prompts
      .flatMap((prompt) =>
        prompt.variables.map((variable, index) =>
          row([
            prompt.variableIdStart + index,
            prompt.templateId,
            variable.name,
            variable.required ? 1 : 0,
            variable.description,
            variable.priority,
          ]),
        ),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `required` = VALUES(`required`),");
  lines.push("    `description` = VALUES(`description`),");
  lines.push("    `priority` = VALUES(`priority`);");
  lines.push("");
};

const row = (values: unknown[]) =>
  `    (${values.map((value) => sqlValue(value)).join(", ")})`;

const sqlValue = (value: unknown): string => {
  if (value === null || value === undefined) {
    return "NULL";
  }
  if (typeof value === "number") {
    return `${value}`;
  }
  if (typeof value === "boolean") {
    return value ? "1" : "0";
  }
  return `'${String(value).replaceAll("\\", "\\\\").replaceAll("'", "''")}'`;
};

const assertUnique = (values: unknown[], name: string) => {
  const seen = new Set<unknown>();
  for (const value of values) {
    if (seen.has(value)) {
      throw new Error(`Duplicate ${name}: ${String(value)}`);
    }
    seen.add(value);
  }
};

const assertPositiveInteger = (value: number, name: string) => {
  if (!Number.isInteger(value) || value <= 0) {
    throw new Error(`${name} must be a positive integer.`);
  }
};

const assertNonBlank = (value: string, name: string) => {
  if (value.trim() === "") {
    throw new Error(`${name} must not be blank.`);
  }
};

main();
