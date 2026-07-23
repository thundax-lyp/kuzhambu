import { createRoot } from "react-dom/client";
import { act } from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { PortalLayout } from "@/components/portal-layout";

const renderLayout = (initialPath = "/") => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter initialEntries={[initialPath]}>
                <Routes>
                    <Route element={<PortalLayout />}>
                        <Route path="/" element={<main>Portal content</main>} />
                        <Route path="/knowledge" element={<main>Knowledge content</main>} />
                        <Route path="/classics/sancai" element={<main>Sancai content</main>} />
                    </Route>
                </Routes>
            </MemoryRouter>
        );
    });

    return { container, root };
};

describe("PortalLayout", () => {
    afterEach(() => {
        document.documentElement.classList.remove("dark");
        window.localStorage.clear();
        document.body.innerHTML = "";
    });

    it("toggles the portal shell theme from the header", () => {
        const { container, root } = renderLayout();
        const themeToggle = container.querySelector<HTMLButtonElement>(
            "[data-testid='portal-header-theme-toggle']"
        );

        expect(themeToggle).not.toBeNull();
        expect(themeToggle?.getAttribute("aria-label")).toBe("切换深色主题");
        expect(document.documentElement.classList.contains("dark")).toBe(false);

        act(() => {
            themeToggle?.click();
        });

        expect(themeToggle?.getAttribute("aria-label")).toBe("切换浅色主题");
        expect(themeToggle?.getAttribute("aria-pressed")).toBe("true");
        expect(document.documentElement.classList.contains("dark")).toBe(true);
        expect(window.localStorage.getItem("kuzhambu.portal.theme")).toBe("dark");

        act(() => {
            root.unmount();
        });
    });

    it("restores the saved dark theme", () => {
        window.localStorage.setItem("kuzhambu.portal.theme", "dark");

        const { container, root } = renderLayout();
        const themeToggle = container.querySelector<HTMLButtonElement>(
            "[data-testid='portal-header-theme-toggle']"
        );

        expect(themeToggle?.getAttribute("aria-label")).toBe("切换浅色主题");
        expect(document.documentElement.classList.contains("dark")).toBe(true);

        act(() => {
            root.unmount();
        });
    });

    it("does not apply the saved dark theme on unsupported routes", () => {
        window.localStorage.setItem("kuzhambu.portal.theme", "dark");

        const { container, root } = renderLayout("/knowledge");

        expect(
            container.querySelector<HTMLButtonElement>("[data-testid='portal-header-theme-toggle']")
        ).toBeNull();
        expect(document.documentElement.classList.contains("dark")).toBe(false);
        expect(window.localStorage.getItem("kuzhambu.portal.theme")).toBe("dark");

        act(() => {
            root.unmount();
        });
    });

    it("keeps the global search and theme entry on the classics reader route", () => {
        const { container, root } = renderLayout("/classics/sancai");

        expect(container.querySelector(".portal-effect-search")).not.toBeNull();
        expect(
            container.querySelector<HTMLButtonElement>("[data-testid='portal-header-theme-toggle']")
        ).not.toBeNull();
        expect(container.querySelector(".portal-effect-layout--reader")).not.toBeNull();
        expect(container.textContent).toContain("Sancai content");

        act(() => {
            root.unmount();
        });
    });

    it("renders a mobile navigation menu trigger with portal navigation links", () => {
        const { container, root } = renderLayout();

        expect(
            container.querySelector<HTMLButtonElement>("[data-testid='portal-header-mobile-menu']")
        ).not.toBeNull();
        expect(container.querySelector(".portal-effect-nav")?.textContent).toContain("三才图会");
        expect(container.querySelector(".portal-effect-nav")?.textContent).toContain("知识图谱");
        expect(container.querySelector(".portal-effect-nav")?.textContent).toContain("问答");

        act(() => {
            root.unmount();
        });
    });

    it("applies the saved dark theme on the classics reader route", () => {
        window.localStorage.setItem("kuzhambu.portal.theme", "dark");

        const { container, root } = renderLayout("/classics/sancai");
        const themeToggle = container.querySelector<HTMLButtonElement>(
            "[data-testid='portal-header-theme-toggle']"
        );

        expect(themeToggle?.getAttribute("aria-label")).toBe("切换浅色主题");
        expect(document.documentElement.classList.contains("dark")).toBe(true);

        act(() => {
            root.unmount();
        });
    });

    it("keeps reader shell behavior on trailing slash classics routes", () => {
        window.localStorage.setItem("kuzhambu.portal.theme", "dark");

        const { container, root } = renderLayout("/classics/sancai/");

        expect(
            container.querySelector<HTMLButtonElement>("[data-testid='portal-header-theme-toggle']")
        ).not.toBeNull();
        expect(container.querySelector(".portal-effect-layout--reader")).not.toBeNull();
        expect(document.documentElement.classList.contains("dark")).toBe(true);
        expect(container.textContent).toContain("Sancai content");

        act(() => {
            root.unmount();
        });
    });
});
