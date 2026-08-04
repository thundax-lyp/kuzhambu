import { readdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
const scriptDir = dirname(fileURLToPath(import.meta.url));
const repoRoot = resolve(scriptDir, "../..");
const sourceRoot = resolve(repoRoot, "db/data-source/ai-prompts");
const outputPath = resolve(repoRoot, "db/data/ai.sql");
const REGISTERED_AT = "2026-02-27 04:00:00.000";
const MYSQL_EPOCH_SHANGHAI = "1970-01-01 08:00:00.000000";
const main = () => {
    const prompts = readPromptSeeds(sourceRoot);
    const sql = generate(prompts);
    if (process.argv.includes("--check")) {
        const current = readFileSync(outputPath, "utf8");
        if (current !== sql) {
            console.error("db/data/ai.sql is out of date. Run: node scripts/seed/generate-ai-sql.mjs");
            process.exit(1);
        }
        return;
    }
    writeFileSync(outputPath, sql);
};
const readPromptSeeds = (root) => {
    const promptDirs = [];
    for (const scope of sortedDirs(root)) {
        for (const prompt of sortedDirs(scope)) {
            promptDirs.push(prompt);
        }
    }
    const prompts = promptDirs.map((promptDir) => {
        const meta = JSON.parse(readFileSync(join(promptDir, "meta.json"), "utf8"));
        return {
            ...meta,
            registeredAt: meta.registeredAt ?? REGISTERED_AT,
            systemTemplate: readFileSync(join(promptDir, "system-template.txt"), "utf8").trim(),
            userTemplate: readFileSync(join(promptDir, "user-template.txt"), "utf8").trim(),
        };
    });
    validatePromptSeeds(prompts);
    return prompts.sort((a, b) => a.priority - b.priority);
};
const sortedDirs = (path) => readdirSync(path)
    .map((name) => join(path, name))
    .filter((child) => statSync(child).isDirectory())
    .sort((a, b) => a.localeCompare(b));
const validatePromptSeeds = (prompts) => {
    assertUnique(prompts.map((prompt) => prompt.priority), "priority");
    assertUnique(prompts.map((prompt) => prompt.capability), "capability");
    for (const prompt of prompts) {
        assertPositiveInteger(prompt.priority, "priority");
        assertPositiveInteger(prompt.versionNo, "versionNo");
        assertNonBlank(prompt.capability, "capability");
        assertNonBlank(prompt.name, "name");
        assertNonBlank(prompt.systemTemplate, "system-template.txt");
        assertNonBlank(prompt.userTemplate, "user-template.txt");
    }
};
const generate = (prompts) => {
    const lines = ["SET NAMES utf8mb4;", ""];
    appendModelSql(lines);
    appendPromptTemplateSql(lines, prompts);
    appendBusinessConfigSql(lines, prompts);
    appendPromptVersionSql(lines, prompts);
    appendPromptVariableSql(lines, prompts);
    return lines.join("\n");
};
const appendModelSql = (lines) => {
    lines.push("INSERT INTO `ai_model` (");
    lines.push("    `id`, `api_source`, `base_url`, `encrypted_api_key`, `model_name`, `display_name`, `capabilities_json`,");
    lines.push("    `default_params_json`, `description`, `enabled`, `registered_at`");
    lines.push(") VALUES");
    lines.push([
        [
            1,
            "OPENAI_COMPATIBLE",
            "",
            null,
            "CTYUN-CX-Qwen3.5-397B-A17B",
            "天翼千问 3.5 397B",
            JSON.stringify(["TEXT2TEXT", "IMAGE2TEXT"]),
            JSON.stringify({ temperature: 0.2, max_tokens: 4096 }),
            "Default OpenAI-compatible vision-capable model for classics AI.",
            1,
            epochMillis(REGISTERED_AT),
        ],
        [
            2,
            "OPENAI_COMPATIBLE",
            "",
            null,
            "CTYUN-bot-DeepSeek-V3.2-pro",
            "天翼 DeepSeek V3.2 Pro",
            JSON.stringify(["TEXT2TEXT"]),
            JSON.stringify({ temperature: 0.2, max_tokens: 4096 }),
            "Default OpenAI-compatible LLM from local server configuration.",
            1,
            epochMillis(REGISTERED_AT),
        ],
        [
            3,
            "BYTEDANCE",
            "",
            null,
            "doubao-seedream-5-0-pro-260628",
            "豆包 Seedream 5.0 Pro",
            JSON.stringify(["TEXT2IMAGE"]),
            JSON.stringify({
                response_format: "url",
                size: "2K",
                stream: false,
                watermark: true,
            }),
            "Default ByteDance text-to-image model.",
            1,
            epochMillis(REGISTERED_AT),
        ],
    ]
        .map((values) => row(values))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `api_source` = VALUES(`api_source`),");
    lines.push("    `base_url` = COALESCE(NULLIF(VALUES(`base_url`), ''), `base_url`),");
    lines.push("    `encrypted_api_key` = COALESCE(VALUES(`encrypted_api_key`), `encrypted_api_key`),");
    lines.push("    `display_name` = VALUES(`display_name`),");
    lines.push("    `capabilities_json` = VALUES(`capabilities_json`),");
    lines.push("    `default_params_json` = VALUES(`default_params_json`),");
    lines.push("    `description` = VALUES(`description`),");
    lines.push("    `enabled` = VALUES(`enabled`),");
    lines.push("    `registered_at` = VALUES(`registered_at`);");
    lines.push("");
};
const appendPromptTemplateSql = (lines, prompts) => {
    lines.push("INSERT INTO `ai_prompt_template` (");
    lines.push("    `id`, `capability`, `name`, `description`, `enabled`,");
    lines.push("    `current_version_no`, `registered_at`");
    lines.push(") VALUES");
    lines.push(prompts
        .map((prompt) => row([
        prompt.priority,
        prompt.capability,
        prompt.name,
        prompt.description,
        prompt.enabled ?? true,
        prompt.current ? prompt.versionNo : null,
        epochMillis(prompt.registeredAt),
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `capability` = VALUES(`capability`),");
    lines.push("    `name` = VALUES(`name`),");
    lines.push("    `description` = VALUES(`description`),");
    lines.push("    `enabled` = VALUES(`enabled`),");
    lines.push("    `current_version_no` = VALUES(`current_version_no`),");
    lines.push("    `registered_at` = VALUES(`registered_at`);");
    lines.push("");
};
const appendBusinessConfigSql = (lines, prompts) => {
    lines.push("INSERT INTO `ai_business_config` (");
    lines.push("    `id`, `capability`, `prompt_template_id`, `model_id`, `default_params_json`, `enabled`, `priority`, `configured_at`");
    lines.push(") VALUES");
    lines.push(prompts
        .map((prompt, index) => row([
        index + 1,
        prompt.capability,
        prompt.priority,
        modelIdFor(prompt.capability),
        null,
        1,
        index + 1,
        epochMillis(prompt.registeredAt),
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `prompt_template_id` = VALUES(`prompt_template_id`),");
    lines.push("    `model_id` = COALESCE(`model_id`, VALUES(`model_id`)),");
    lines.push("    `default_params_json` = VALUES(`default_params_json`),");
    lines.push("    `enabled` = VALUES(`enabled`),");
    lines.push("    `priority` = VALUES(`priority`),");
    lines.push("    `configured_at` = VALUES(`configured_at`);");
    lines.push("");
};
const modelIdFor = (capability) => {
    switch (capability) {
        case "classics_image_describe":
            return 1;
        case "classics_image_generate":
            return 3;
        default:
            return 2;
    }
};
const appendPromptVersionSql = (lines, prompts) => {
    lines.push("INSERT INTO `ai_prompt_version` (");
    lines.push("    `id`, `template_id`, `version_no`, `message_templates_json`,");
    lines.push("    `variables_snapshot_json`, `output_schema_json`, `change_summary`, `registered_at`");
    lines.push(") VALUES");
    lines.push(prompts
        .map((prompt) => row([
        prompt.priority,
        prompt.priority,
        prompt.versionNo,
        JSON.stringify([
            { role: "system", content: prompt.systemTemplate },
            { role: "user", content: prompt.userTemplate },
        ]),
        JSON.stringify(prompt.variables.map(({ name, required, description }) => ({
            variableName: name,
            required,
            description,
        }))),
        JSON.stringify(prompt.outputSchema),
        prompt.changeSummary,
        epochMillis(prompt.registeredAt),
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `message_templates_json` = VALUES(`message_templates_json`),");
    lines.push("    `variables_snapshot_json` = VALUES(`variables_snapshot_json`),");
    lines.push("    `output_schema_json` = VALUES(`output_schema_json`),");
    lines.push("    `change_summary` = VALUES(`change_summary`),");
    lines.push("    `registered_at` = VALUES(`registered_at`);");
    lines.push("");
};
const appendPromptVariableSql = (lines, prompts) => {
    lines.push("INSERT INTO `ai_prompt_variable` (");
    lines.push("    `id`, `template_id`, `variable_name`, `required`, `description`, `priority`");
    lines.push(") VALUES");
    lines.push(prompts
        .flatMap((prompt) => prompt.variables.map((variable) => ({ prompt, variable })))
        .map(({ prompt, variable }, index) => row([
        index + 1,
        prompt.priority,
        variable.name,
        variable.required ? 1 : 0,
        variable.description,
        index + 1,
    ]))
        .join(",\n"));
    lines.push("ON DUPLICATE KEY UPDATE");
    lines.push("    `required` = VALUES(`required`),");
    lines.push("    `description` = VALUES(`description`),");
    lines.push("    `priority` = VALUES(`priority`);");
    lines.push("");
};
const row = (values) => `    (${values.map((value) => sqlValue(value)).join(", ")})`;
const sqlValue = (value) => {
    if (value === null || value === undefined) {
        return "NULL";
    }
    if (typeof value === "number") {
        return `${value}`;
    }
    if (typeof value === "boolean") {
        return value ? "1" : "0";
    }
    if (isRawSql(value)) {
        return value.rawSql;
    }
    return `'${String(value).replaceAll("\\", "\\\\").replaceAll("'", "''")}'`;
};
const epochMillis = (displayTime) => ({
    rawSql: `TIMESTAMPDIFF(MICROSECOND, '${MYSQL_EPOCH_SHANGHAI}', '${normalizeDisplayTime(displayTime)}') DIV 1000`,
});
const normalizeDisplayTime = (displayTime) => {
    const text = displayTime.trim().replace("T", " ");
    const match = text.match(/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2})(?:\.(\d{1,6}))?$/);
    if (!match) {
        throw new Error(`Invalid Asia/Shanghai display datetime: ${displayTime}`);
    }
    return `${match[1]}.${(match[2] ?? "").padEnd(6, "0")}`;
};
const isRawSql = (value) => typeof value === "object" &&
    value !== null &&
    "rawSql" in value &&
    typeof value.rawSql === "string";
const assertUnique = (values, name) => {
    const seen = new Set();
    for (const value of values) {
        if (seen.has(value)) {
            throw new Error(`Duplicate ${name}: ${String(value)}`);
        }
        seen.add(value);
    }
};
const assertPositiveInteger = (value, name) => {
    if (!Number.isInteger(value) || value <= 0) {
        throw new Error(`${name} must be a positive integer.`);
    }
};
const assertNonBlank = (value, name) => {
    if (value.trim() === "") {
        throw new Error(`${name} must not be blank.`);
    }
};
main();
