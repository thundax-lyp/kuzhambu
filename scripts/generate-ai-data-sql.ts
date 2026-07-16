import { readdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

type JsonObject = Record<string, unknown>;

type AiPromptMeta = {
  templateId: number;
  promptVersionId: number;
  variableIdStart: number;
  scope: string;
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
    prompts.map((prompt) => `${prompt.scope}:${prompt.capability}`),
    "scope/capability",
  );
  for (const prompt of prompts) {
    assertPositiveInteger(prompt.templateId, "templateId");
    assertPositiveInteger(prompt.promptVersionId, "promptVersionId");
    assertPositiveInteger(prompt.variableIdStart, "variableIdStart");
    assertPositiveInteger(prompt.versionNo, "versionNo");
    assertNonBlank(prompt.scope, "scope");
    assertNonBlank(prompt.capability, "capability");
    assertNonBlank(prompt.name, "name");
    assertNonBlank(prompt.systemTemplate, "system-template.txt");
    assertNonBlank(prompt.userTemplate, "user-template.txt");
  }
};

const generate = (prompts: PromptSeed[]) => {
  const lines: string[] = ["SET NAMES utf8mb4;", ""];
  appendCapabilitySql(lines);
  appendServiceConfigSql(lines);
  appendModelSql(lines);
  appendCapabilityMappingSql(lines);
  appendActionStatusSql(lines);
  appendPromptTemplateSql(lines, prompts);
  appendPromptVersionSql(lines, prompts);
  appendPromptVariableSql(lines, prompts);
  return lines.join("\n");
};

const appendCapabilitySql = (lines: string[]) => {
  lines.push("INSERT INTO `ai_capability` (");
  lines.push(
    "    `capability`, `name`, `required_tags_json`, `output_mode`, `enabled`, `priority`",
  );
  lines.push(") VALUES");
  lines.push(
    [
      ["translate", "古文翻译", ["text"], "TEXT", 1, 10],
      ["tags", "标签提取", ["text", "structured_output"], "STRUCTURED", 1, 20],
      ["visual", "视觉描述", ["text"], "TEXT", 1, 30],
      ["fusion", "信息融合", ["text"], "TEXT", 1, 40],
      ["qa", "问答生成", ["text", "structured_output"], "STRUCTURED", 1, 50],
      ["split", "条目拆分", ["text", "structured_output"], "STRUCTURED", 1, 60],
      ["image_analysis", "图片理解", ["vision"], "MARKDOWN", 1, 70],
      ["image_gen", "图片生成", ["image_gen"], "ARTIFACT", 1, 80],
      [
        "knowledge_graph",
        "知识图谱抽取",
        ["text", "structured_output"],
        "STRUCTURED",
        1,
        90,
      ],
      ["summary", "摘要生成", ["text"], "TEXT", 1, 100],
      ["version_summary", "版本摘要", ["text"], "TEXT", 1, 110],
      [
        "query_understanding",
        "查询理解",
        ["text", "structured_output"],
        "STRUCTURED",
        1,
        120,
      ],
      [
        "answer_generation",
        "回答生成",
        ["text", "streaming_text"],
        "TEXT",
        1,
        130,
      ],
      [
        "relation_extraction",
        "实体关系抽取",
        ["text", "structured_output"],
        "STRUCTURED",
        1,
        140,
      ],
      [
        "lineage_extraction",
        "世系图抽取",
        ["text", "structured_output"],
        "STRUCTURED",
        1,
        150,
      ],
      ["prompt_suggestion", "提示词优化建议", ["text"], "TEXT", 1, 160],
    ]
      .map(([capability, name, tags, outputMode, enabled, priority]) =>
        row([
          capability,
          name,
          JSON.stringify(tags),
          outputMode,
          enabled,
          priority,
        ]),
      )
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `name` = VALUES(`name`),");
  lines.push("    `required_tags_json` = VALUES(`required_tags_json`),");
  lines.push("    `output_mode` = VALUES(`output_mode`),");
  lines.push("    `enabled` = VALUES(`enabled`),");
  lines.push("    `priority` = VALUES(`priority`);");
  lines.push("");
};

const appendServiceConfigSql = (lines: string[]) => {
  lines.push(
    "-- Runtime endpoint secrets are synchronized from dev.env by scripts/sync-ai-service-config.sh.",
  );
  lines.push("INSERT INTO `ai_service_config` (");
  lines.push(
    "    `service_id`, `service_role`, `api_source`, `base_url`, `encrypted_api_key`,",
  );
  lines.push("    `enabled`, `status`, `last_checked_at`, `configured_at`");
  lines.push(") VALUES");
  lines.push(
    [
      [
        900001,
        "PRIMARY",
        "OPENAI_COMPATIBLE",
        "",
        null,
        1,
        "AVAILABLE",
        REGISTERED_AT,
        REGISTERED_AT,
      ],
      [
        900002,
        "TEXT2IMAGE",
        "OPENAI_COMPATIBLE",
        "",
        null,
        1,
        "AVAILABLE",
        REGISTERED_AT,
        REGISTERED_AT,
      ],
    ]
      .map((values) => row(values))
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `api_source` = VALUES(`api_source`),");
  lines.push(
    "    `base_url` = COALESCE(NULLIF(VALUES(`base_url`), ''), `base_url`),",
  );
  lines.push(
    "    `encrypted_api_key` = COALESCE(VALUES(`encrypted_api_key`), `encrypted_api_key`),",
  );
  lines.push("    `enabled` = VALUES(`enabled`),");
  lines.push("    `status` = VALUES(`status`),");
  lines.push("    `last_checked_at` = VALUES(`last_checked_at`),");
  lines.push("    `configured_at` = VALUES(`configured_at`);");
  lines.push("");
};

const appendModelSql = (lines: string[]) => {
  lines.push("INSERT INTO `ai_model` (");
  lines.push(
    "    `model_id`, `service_id`, `model_name`, `display_name`, `capability_tags_json`,",
  );
  lines.push(
    "    `default_params_json`, `description`, `enabled`, `registered_at`",
  );
  lines.push(") VALUES");
  lines.push(
    [
      [
        900101,
        900001,
        "CTYUN-CX-Qwen3.5-397B-A17B",
        "CTYUN Qwen3.5 397B",
        JSON.stringify([
          "text",
          "vision",
          "structured_output",
          "streaming_text",
        ]),
        JSON.stringify({ temperature: 0.2, max_tokens: 4096 }),
        "Default OpenAI-compatible vision-capable model for classics AI.",
        1,
        REGISTERED_AT,
      ],
      [
        900102,
        900001,
        "CTYUN-bot-DeepSeek-V3.2-pro",
        "CTYUN DeepSeek V3.2 Pro",
        JSON.stringify(["text", "structured_output", "streaming_text"]),
        JSON.stringify({ temperature: 0.2, max_tokens: 4096 }),
        "Default OpenAI-compatible LLM from local server configuration.",
        1,
        REGISTERED_AT,
      ],
      [
        900201,
        900002,
        "doubao-seedream-5-0-pro-260628",
        "Doubao Seedream 5.0 Pro",
        JSON.stringify(["image_gen"]),
        JSON.stringify({
          response_format: "url",
          size: "2K",
          stream: false,
          watermark: true,
        }),
        "Default OpenAI-compatible text-to-image model from local server configuration.",
        1,
        REGISTERED_AT,
      ],
    ]
      .map((values) => row(values))
      .join(",\n"),
  );
  lines.push("ON DUPLICATE KEY UPDATE");
  lines.push("    `service_id` = VALUES(`service_id`),");
  lines.push("    `display_name` = VALUES(`display_name`),");
  lines.push("    `capability_tags_json` = VALUES(`capability_tags_json`),");
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
      [910101, "classics", "summary", 900102, 1, REGISTERED_AT],
      [910102, "classics", "tags", 900102, 1, REGISTERED_AT],
      [910103, "classics", "qa", 900102, 1, REGISTERED_AT],
      [910104, "classics", "image_analysis", 900101, 1, REGISTERED_AT],
      [910105, "classics", "translate", 900102, 1, REGISTERED_AT],
      [910106, "classics", "image_gen", 900201, 1, REGISTERED_AT],
      [910107, "classics", "fusion", 900102, 1, REGISTERED_AT],
      [910108, "classics", "visual", 900102, 1, REGISTERED_AT],
      [910201, "discovery", "query_understanding", 900102, 1, REGISTERED_AT],
      [910202, "discovery", "answer_generation", 900102, 1, REGISTERED_AT],
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
      [920101, "classics", "summary", 1, null, REGISTERED_AT],
      [920102, "classics", "tags", 1, null, REGISTERED_AT],
      [920103, "classics", "qa", 1, null, REGISTERED_AT],
      [920104, "classics", "image_analysis", 1, null, REGISTERED_AT],
      [920105, "classics", "translate", 1, null, REGISTERED_AT],
      [920106, "classics", "image_gen", 1, null, REGISTERED_AT],
      [920107, "classics", "fusion", 1, null, REGISTERED_AT],
      [920108, "classics", "visual", 1, null, REGISTERED_AT],
      [920201, "discovery", "query_understanding", 1, null, REGISTERED_AT],
      [920202, "discovery", "answer_generation", 1, null, REGISTERED_AT],
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
    "    `template_id`, `scope`, `capability`, `name`, `description`, `status`,",
  );
  lines.push("    `current_version_no`, `registered_at`");
  lines.push(") VALUES");
  lines.push(
    prompts
      .map((prompt) =>
        row([
          prompt.templateId,
          prompt.scope,
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
  lines.push("    `scope` = VALUES(`scope`),");
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
    "    `prompt_version_id`, `template_id`, `version_no`, `message_templates_json`,",
  );
  lines.push(
    "    `variables_snapshot_json`, `output_schema_json`, `current_key`, `change_summary`, `registered_at`",
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
          prompt.current ? `${prompt.templateId}:current` : null,
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
  lines.push("    `current_key` = VALUES(`current_key`),");
  lines.push("    `change_summary` = VALUES(`change_summary`),");
  lines.push("    `registered_at` = VALUES(`registered_at`);");
  lines.push("");
};

const appendPromptVariableSql = (lines: string[], prompts: PromptSeed[]) => {
  lines.push("INSERT INTO `ai_prompt_variable` (");
  lines.push(
    "    `variable_id`, `template_id`, `variable_name`, `required`, `description`, `priority`",
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
