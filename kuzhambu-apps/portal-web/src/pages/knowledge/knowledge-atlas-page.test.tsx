import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { KnowledgeAtlasPage } from "./knowledge-atlas-page";

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter>
                <KnowledgeAtlasPage />
            </MemoryRouter>
        );
    });

    return { container, root };
};

describe("KnowledgeAtlasPage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("shows the filter browse detail structure", () => {
        const { container, root } = renderPage();

        expect(container.textContent).toContain("图谱浏览台");
        expect(container.textContent).toContain("筛选");
        expect(container.textContent).toContain("浏览");
        expect(container.textContent).toContain("详情");
        expect(container.textContent).toContain("黄帝");

        const links = Array.from(container.querySelectorAll("a")).map((link) =>
            link.getAttribute("href")
        );
        expect(links).toContain("/knowledge");

        act(() => {
            root.unmount();
        });
    });
});
