import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { KuzhambuRichContentViewer } from "./kuzhambu-rich-content-viewer";

describe("KuzhambuRichContentViewer", () => {
    it("renders markdown content", () => {
        render(<KuzhambuRichContentViewer content={"## 元旦朝贺\n\n**礼制**"} format="MARKDOWN" />);

        expect(screen.getByRole("heading", { name: "元旦朝贺" })).toBeInTheDocument();
        expect(screen.getByText("礼制")).toBeInTheDocument();
        expect(screen.getByText("礼制").tagName).toBe("STRONG");
    });

    it("renders sanitized html content", () => {
        const { container } = render(
            <KuzhambuRichContentViewer
                content={
                    '<p>上元灯市</p><img src="x" onerror="alert(1)" /><script>alert(1)</script>'
                }
                format="HTML"
            />
        );

        expect(screen.getByText("上元灯市")).toBeInTheDocument();
        expect(container.querySelector("script")).not.toBeInTheDocument();
        expect(container.querySelector("img")?.getAttribute("onerror")).toBeNull();
    });

    it("renders text content without treating it as html", () => {
        const { container } = render(
            <KuzhambuRichContentViewer content={"<strong>正旦</strong>\n拜礼"} format="TEXT" />
        );

        expect(container.textContent).toContain("<strong>正旦</strong>");
        expect(container.textContent).toContain("拜礼");
        expect(container.querySelector("strong")).not.toBeInTheDocument();
    });
});
