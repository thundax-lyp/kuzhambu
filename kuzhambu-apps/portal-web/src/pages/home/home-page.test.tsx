import { createRoot } from "react-dom/client";
import { act } from "react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { HomePage } from "./home-page";

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter>
                <HomePage />
            </MemoryRouter>
        );
    });

    return { container, root };
};

describe("HomePage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("exposes discovery navigation entry points", () => {
        const { container, root } = renderPage();

        const links = Array.from(container.querySelectorAll("a")).map((link) => ({
            href: link.getAttribute("href"),
            text: link.textContent
        }));

        expect(links).toEqual(
            expect.arrayContaining([
                expect.objectContaining({ href: "/classics/sancai", text: "进入三才图会" }),
                expect.objectContaining({ href: "/knowledge", text: "进入知识馆" }),
                expect.objectContaining({ href: "/discovery/search", text: "进入检索" }),
                expect.objectContaining({ href: "/discovery/qa", text: "进入问答" }),
                expect.objectContaining({
                    href: "/classics/sancai",
                    text: "三才图会浏览已发布的三才图会条目、译文与配图"
                }),
                expect.objectContaining({
                    href: "/knowledge",
                    text: "知识图谱馆进入知识首页，查看图谱、质量与来源导览"
                }),
                expect.objectContaining({
                    href: "/discovery/search",
                    text: "知识检索围绕实体、标签和关系组织检索"
                }),
                expect.objectContaining({
                    href: "/discovery/qa",
                    text: "问答工作台先建会话，再看来源、轨迹与回答"
                })
            ])
        );

        act(() => {
            root.unmount();
        });
    });
});
