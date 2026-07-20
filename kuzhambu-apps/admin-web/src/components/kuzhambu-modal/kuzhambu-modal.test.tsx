import { render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KuzhambuModal } from "./kuzhambu-modal";

describe("KuzhambuModal", () => {
    afterEach(() => {
        vi.unstubAllEnvs();
    });

    it("uses the required testId prop as the technical anchor", () => {
        render(
            <KuzhambuModal open testId="kuzhambu-modal-editor-modal" title="编辑">
                内容
            </KuzhambuModal>
        );

        expect(screen.getByTestId("kuzhambu-modal-editor-modal")).toBeInTheDocument();
    });

    it("does not expose testId in production unless explicitly enabled", () => {
        vi.stubEnv("PROD", true);

        render(
            <KuzhambuModal open testId="kuzhambu-modal-editor-modal" title="编辑">
                内容
            </KuzhambuModal>
        );

        expect(screen.queryByTestId("kuzhambu-modal-editor-modal")).not.toBeInTheDocument();
    });

    it("can expose testId in production for explicit test builds", () => {
        vi.stubEnv("PROD", true);
        vi.stubEnv("VITE_EXPOSE_TEST_ID", "true");

        render(
            <KuzhambuModal open testId="kuzhambu-modal-editor-modal" title="编辑">
                内容
            </KuzhambuModal>
        );

        expect(screen.getByTestId("kuzhambu-modal-editor-modal")).toBeInTheDocument();
    });
});
