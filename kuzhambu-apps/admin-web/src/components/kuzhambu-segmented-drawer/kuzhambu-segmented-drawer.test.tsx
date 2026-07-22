import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { KuzhambuSegmentedDrawer } from "./kuzhambu-segmented-drawer";

describe("KuzhambuSegmentedDrawer", () => {
    it("renders the active section and switches through the segmented control", async () => {
        const user = userEvent.setup();
        const onSectionChange = vi.fn();

        render(
            <KuzhambuSegmentedDrawer
                activeSection="basic"
                open
                sections={[
                    { label: "基础信息", value: "basic", content: "基础内容" },
                    { label: "标签", value: "tags", content: "标签内容" }
                ]}
                testId="kuzhambu-segmented-drawer-editor-drawer"
                title="编辑"
                onClose={vi.fn()}
                onSectionChange={onSectionChange}
            />
        );

        expect(screen.getByText("基础内容")).toBeInTheDocument();

        await user.click(screen.getByText("标签"));

        expect(onSectionChange).toHaveBeenCalledWith("tags");
    });

    it("does not render hidden sections", () => {
        render(
            <KuzhambuSegmentedDrawer
                activeSection="basic"
                open
                sections={[
                    { label: "基础信息", value: "basic", content: "基础内容" },
                    { label: "版本", value: "versions", content: "版本内容", visible: false }
                ]}
                testId="kuzhambu-segmented-drawer-editor-drawer"
                title="编辑"
                onClose={vi.fn()}
                onSectionChange={vi.fn()}
            />
        );

        expect(screen.getByText("基础内容")).toBeInTheDocument();
        expect(screen.queryByText("版本")).not.toBeInTheDocument();
        expect(screen.queryByText("版本内容")).not.toBeInTheDocument();
    });
});
