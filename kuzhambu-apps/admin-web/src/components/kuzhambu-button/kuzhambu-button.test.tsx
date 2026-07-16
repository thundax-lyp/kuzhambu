import { render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KuzhambuButton } from "./kuzhambu-button";

describe("KuzhambuButton", () => {
    afterEach(() => {
        vi.unstubAllEnvs();
    });

    it("uses the required testId prop as the technical anchor", () => {
        render(<KuzhambuButton testId="kuzhambu-button-share-button">分享</KuzhambuButton>);

        expect(screen.getByTestId("kuzhambu-button-share-button")).toBeInTheDocument();
    });

    it("maps ariaLabel to the accessible button name when business semantics need it", () => {
        render(
            <KuzhambuButton ariaLabel="分享条目" testId="kuzhambu-button-share-entry-button">
                分享
            </KuzhambuButton>
        );

        expect(screen.getByRole("button", { name: "分享条目" })).toBeInTheDocument();
    });

    it("does not expose testId in production unless explicitly enabled", () => {
        vi.stubEnv("PROD", true);

        render(<KuzhambuButton testId="kuzhambu-button-share-button">分享</KuzhambuButton>);

        expect(screen.queryByTestId("kuzhambu-button-share-button")).not.toBeInTheDocument();
    });

    it("can expose testId in production for explicit test builds", () => {
        vi.stubEnv("PROD", true);
        vi.stubEnv("VITE_EXPOSE_TEST_ID", "true");

        render(<KuzhambuButton testId="kuzhambu-button-share-button">分享</KuzhambuButton>);

        expect(screen.getByTestId("kuzhambu-button-share-button")).toBeInTheDocument();
    });
});
