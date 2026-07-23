import { render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { KuzhambuTextCompare } from "./kuzhambu-text-compare";

describe("KuzhambuTextCompare", () => {
    afterEach(() => {
        vi.unstubAllEnvs();
    });

    it("renders added and removed text", () => {
        render(
            <KuzhambuTextCompare baseline="天地玄黄" candidate="天地玄妙" testId="text-compare" />
        );

        expect(screen.getByTestId("text-compare").querySelector(".is-removed")).toHaveTextContent(
            "黄"
        );
        expect(screen.getByTestId("text-compare").querySelector(".is-added")).toHaveTextContent(
            "妙"
        );
    });

    it("renders empty text when content is the same", () => {
        render(<KuzhambuTextCompare baseline="同文" candidate="同文" emptyText="无变化" />);

        expect(screen.getByText("无变化")).toBeInTheDocument();
    });

    it("does not expose testId in production unless explicitly enabled", () => {
        vi.stubEnv("PROD", true);

        render(<KuzhambuTextCompare baseline="甲" candidate="乙" testId="kuzhambu-text-compare" />);

        expect(screen.queryByTestId("kuzhambu-text-compare")).not.toBeInTheDocument();
    });

    it("can expose testId in production for explicit test builds", () => {
        vi.stubEnv("PROD", true);
        vi.stubEnv("VITE_EXPOSE_TEST_ID", "true");

        render(<KuzhambuTextCompare baseline="甲" candidate="乙" testId="kuzhambu-text-compare" />);

        expect(screen.getByTestId("kuzhambu-text-compare")).toBeInTheDocument();
    });
});
