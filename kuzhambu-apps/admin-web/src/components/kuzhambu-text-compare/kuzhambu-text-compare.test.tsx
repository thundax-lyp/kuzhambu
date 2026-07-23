import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuTextCompare } from "./kuzhambu-text-compare";

describe("KuzhambuTextCompare", () => {
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
});
