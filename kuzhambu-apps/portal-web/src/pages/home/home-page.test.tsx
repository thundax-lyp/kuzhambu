import { createRoot } from "react-dom/client";
import { act } from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { PortalLayout } from "@/components/portal-layout";
import { HomePage } from "./home-page";

const renderPage = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter>
                <Routes>
                    <Route element={<PortalLayout />}>
                        <Route path="/" element={<HomePage />} />
                    </Route>
                </Routes>
            </MemoryRouter>
        );
    });

    return { container, root };
};

describe("HomePage", () => {
    afterEach(() => {
        document.body.innerHTML = "";
    });

    it("exposes content portal navigation entry points", () => {
        const { container, root } = renderPage();

        const links = Array.from(container.querySelectorAll("a")).map((link) => ({
            href: link.getAttribute("href"),
            text: link.textContent
        }));

        expect(links).toEqual(
            expect.arrayContaining([
                expect.objectContaining({ href: "/classics/sancai", text: "浏览三才图会" }),
                expect.objectContaining({ href: "/knowledge", text: "知识图谱" }),
                expect.objectContaining({
                    href: "/discovery/search",
                    text: "搜索条目、图像、人物、地名、典籍...搜索"
                }),
                expect.objectContaining({ href: "/discovery/search", text: "进入知识检索" }),
                expect.objectContaining({ href: "/discovery/qa", text: "问答" }),
                expect.objectContaining({
                    href: "/classics/sancai",
                    text: "凤凰三才图会 · 鸟兽 · 百鸟部黄帝时，凤凰集于岐山之阳，其羽五色各异……05-18"
                }),
                expect.objectContaining({
                    href: "/knowledge/atlas",
                    text: "古都图志历代都城图景与建置沿革，考镜古今。"
                }),
                expect.objectContaining({
                    href: "/discovery/search",
                    text: "长安与洛阳：两京建置与城市格局比较86 条相关"
                }),
                expect.objectContaining({ href: "/knowledge", text: "关于KUZHAMBU" })
            ])
        );

        act(() => {
            root.unmount();
        });
    });
});
