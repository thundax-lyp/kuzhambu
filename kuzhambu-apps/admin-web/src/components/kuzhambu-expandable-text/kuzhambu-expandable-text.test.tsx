import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuExpandableText } from "./kuzhambu-expandable-text";

describe("KuzhambuExpandableText", () => {
    it("renders fallback text for blank content", () => {
        render(<KuzhambuExpandableText content=" " emptyText="暂无内容" />);

        expect(screen.getByText("暂无内容")).toBeInTheDocument();
    });

    it("renders content through the expandable text wrapper", () => {
        render(
            <KuzhambuExpandableText
                className="custom-expandable-text"
                content={"第一行\n第二行\n第三行"}
                collapsedRows={2}
                testId="expandable-text"
            />
        );

        const text = screen.getByTestId("expandable-text");
        expect(text).toHaveClass("kuzhambu-expandable-text");
        expect(text).toHaveClass("custom-expandable-text");
        expect(text).toHaveTextContent("第一行");
        expect(text).toHaveTextContent("第二行");
        expect(text).toHaveTextContent("第三行");
    });
});
