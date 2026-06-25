import { act } from "react";
import { createRoot } from "react-dom/client";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { KnowledgeHomePage } from "./knowledge-home-page";

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter>
                <KnowledgeHomePage />
            </MemoryRouter>
        );
    });

    return { container, root };
};

describe("KnowledgeHomePage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("shows the atlas and quality entry points", () => {
        const { container, root } = renderPage();

        expect(container.textContent).toContain("古籍知识图谱馆");
        expect(container.textContent).toContain("图谱浏览");
        expect(container.textContent).toContain("质量总览");

        const links = Array.from(container.querySelectorAll("a")).map((link) => ({
            href: link.getAttribute("href"),
            text: link.textContent
        }));

        expect(links).toEqual(
            expect.arrayContaining([
                expect.objectContaining({ href: "/knowledge/atlas", text: "进入图谱浏览" }),
                expect.objectContaining({ href: "/knowledge/quality", text: "查看质量总览" }),
                expect.objectContaining({
                    href: "/knowledge/atlas",
                    text: "图谱浏览进入关系画布，沿实体、关系与时间线展开阅读。"
                }),
                expect.objectContaining({
                    href: "/knowledge/quality",
                    text: "质量总览查看确认率、来源构成与当前待处理事项。"
                })
            ])
        );

        act(() => {
            root.unmount();
        });
    });
});
