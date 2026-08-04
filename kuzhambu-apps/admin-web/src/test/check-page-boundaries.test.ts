import { execFileSync } from "node:child_process";
import { mkdtempSync, mkdirSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";

const SCRIPT_PATH = resolve("scripts/check-page-boundaries.mjs");

const createFixture = () => mkdtempSync(join(tmpdir(), "kuzhambu-page-boundaries-"));

const runGate = (pagesRoot: string) => {
    try {
        execFileSync(process.execPath, [SCRIPT_PATH, pagesRoot], {
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

describe("check-page-boundaries", () => {
    it("rejects a page entry whose name does not match its domain", () => {
        const fixtureRoot = createFixture();
        try {
            const domainPath = join(fixtureRoot, "demo", "article");
            mkdirSync(domainPath, { recursive: true });
            writeFileSync(join(domainPath, "wrong-page.tsx"), "export {};\n");

            const result = runGate(fixtureRoot);

            expect(result.exitCode).toBe(1);
            expect(result.output).toContain("ADMIN_WEB_PATH_PAGE_SHAPE");
            expect(result.output).toContain("article-page.tsx");
        } finally {
            rmSync(fixtureRoot, { recursive: true, force: true });
        }
    });

    it("ignores a shared domain without a page entry", () => {
        const fixtureRoot = createFixture();
        try {
            const sharedPath = join(fixtureRoot, "demo", "common");
            mkdirSync(sharedPath, { recursive: true });
            writeFileSync(join(sharedPath, "shared-service.ts"), "export {};\n");

            expect(runGate(fixtureRoot)).toEqual({ exitCode: 0, output: "" });
        } finally {
            rmSync(fixtureRoot, { recursive: true, force: true });
        }
    });
});
