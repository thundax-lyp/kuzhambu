import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { KnowledgeQualityPage } from "./knowledge-quality-page";

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter>
                <KnowledgeQualityPage />
            </MemoryRouter>
        );
    });

    return { container, root };
};

describe("KnowledgeQualityPage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("shows stats trends issues and source details", () => {
        const { container, root } = renderPage();

        expect(container.textContent).toContain("质量总览台");
        expect(container.textContent).toContain("实体确认率");
        expect(container.textContent).toContain("近期变化");
        expect(container.textContent).toContain("建议优先处理");
        expect(container.textContent).toContain("最近来源快照");

        const links = Array.from(container.querySelectorAll("a")).map((link) =>
            link.getAttribute("href")
        );
        expect(links).toContain("/knowledge");

        act(() => {
            root.unmount();
        });
    });
});
