import { execFileSync } from "node:child_process";
import { mkdtempSync, mkdirSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";

const SCRIPT_PATH = resolve("scripts/check-type-boundaries.mjs");

const createFixture = () => mkdtempSync(join(tmpdir(), "kuzhambu-type-boundaries-"));

const runGate = (sourceRoot: string) => {
    try {
        execFileSync(process.execPath, [SCRIPT_PATH, sourceRoot], {
            encoding: "utf8",
            stdio: "pipe"
        });
        return { exitCode: 0, output: "" };
    } catch (error) {
        const failure = error as { status?: number; stderr?: string; stdout?: string };
        return {
            exitCode: failure.status ?? 1,
            output: `${failure.stdout ?? ""}${failure.stderr ?? ""}`
        };
    }
};

describe("check-type-boundaries", () => {
    const formValuesName = ["Article", "Form", "Values"].join("");

    it("rejects form values declared in a non-form page component", () => {
        const fixtureRoot = createFixture();
        try {
            const componentPath = join(fixtureRoot, "pages", "demo", "article", "article-table");
            mkdirSync(componentPath, { recursive: true });
            writeFileSync(
                join(componentPath, "article-table.tsx"),
                `export interface ${formValuesName} { title: string; }\n`
            );

            const result = runGate(fixtureRoot);
            expect(result.exitCode).toBe(1);
            expect(result.output).toContain("ADMIN_WEB_NAME_FORM_VALUES_LOCATION");
        } finally {
            rmSync(fixtureRoot, { recursive: true, force: true });
        }
    });

    it("allows form values declared by the actual form component", () => {
        const fixtureRoot = createFixture();
        try {
            const componentPath = join(fixtureRoot, "pages", "demo", "article", "article-form");
            mkdirSync(componentPath, { recursive: true });
            writeFileSync(
                join(componentPath, "article-form.tsx"),
                `export interface ${formValuesName} { title: string; }\nconst form = Form.useForm;\n`
            );

            expect(runGate(fixtureRoot)).toEqual({ exitCode: 0, output: "" });
        } finally {
            rmSync(fixtureRoot, { recursive: true, force: true });
        }
    });
});
