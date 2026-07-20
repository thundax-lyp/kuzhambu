import { render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KuzhambuDrawer } from "./kuzhambu-drawer";

describe("KuzhambuDrawer", () => {
    afterEach(() => {
        vi.unstubAllEnvs();
    });

    it("uses the required testId prop as the technical anchor", () => {
        render(
            <KuzhambuDrawer open testId="kuzhambu-drawer-editor-drawer" title="编辑">
                内容
            </KuzhambuDrawer>
        );

        expect(screen.getByTestId("kuzhambu-drawer-editor-drawer")).toBeInTheDocument();
    });

    it("does not expose testId in production unless explicitly enabled", () => {
        vi.stubEnv("PROD", true);

        render(
            <KuzhambuDrawer open testId="kuzhambu-drawer-editor-drawer" title="编辑">
                内容
            </KuzhambuDrawer>
        );

        expect(screen.queryByTestId("kuzhambu-drawer-editor-drawer")).not.toBeInTheDocument();
    });

    it("can expose testId in production for explicit test builds", () => {
        vi.stubEnv("PROD", true);
        vi.stubEnv("VITE_EXPOSE_TEST_ID", "true");

        render(
            <KuzhambuDrawer open testId="kuzhambu-drawer-editor-drawer" title="编辑">
                内容
            </KuzhambuDrawer>
        );

        expect(screen.getByTestId("kuzhambu-drawer-editor-drawer")).toBeInTheDocument();
    });
});
