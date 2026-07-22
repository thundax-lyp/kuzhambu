import { createRoot } from "react-dom/client";
import { act } from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import { PortalLayout } from "@/components/portal-layout";

const renderLayout = () => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    const root = createRoot(container);

    act(() => {
        root.render(
            <MemoryRouter>
                <Routes>
                    <Route element={<PortalLayout />}>
                        <Route path="/" element={<main>Portal content</main>} />
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
});
