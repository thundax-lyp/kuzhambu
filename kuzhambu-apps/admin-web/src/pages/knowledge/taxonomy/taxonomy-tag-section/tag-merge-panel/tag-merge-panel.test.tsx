import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { TagMergePanel } from "./tag-merge-panel";

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

describe("TagMergePanel", () => {
    afterEach(() => {
        cleanup();
    });

    it("previews and applies merge actions with selected tags", async () => {
        const onPreview = vi.fn();
        const onApply = vi.fn();

        render(
            <TagMergePanel
                applying={false}
                canEditTag
                preview={{
                    sourceTag: { id: "1001", name: "礼制" },
                    targetTag: { id: "1002", name: "祭祀" },
                    aliasesToMerge: [{ id: "2001", name: "礼典", source: "MANUAL" }],
                    impactedContentRefs: [
                        {
                            id: "3001",
                            contentTitle: "周礼",
                            contentType: "CLASSICS",
                            source: "MANUAL"
                        }
                    ],
                    pendingReviewCount: 1,
                    governedRecordCount: 3
                }}
                previewing={false}
                tags={[
                    { id: "1001", name: "礼制" },
                    { id: "1002", name: "祭祀" }
                ]}
                onApply={onApply}
                onPreview={onPreview}
            />
        );

        await openSelectAndChoose("源标签", "礼制（1001）");
        await openSelectAndChoose("目标标签", "祭祀（1002）");
        await userEvent.click(screen.getByRole("button", { name: "预览合并影响" }));
        await userEvent.click(screen.getByRole("button", { name: "执行标签合并" }));

        expect(onPreview).toHaveBeenCalledWith({
            sourceTagId: "1001",
            targetTagId: "1002"
        });
        expect(onApply).toHaveBeenCalledWith({
            sourceTagId: "1001",
            targetTagId: "1002"
        });
        expect(screen.getByText("礼典")).toBeInTheDocument();
        expect(screen.getByText("周礼 · CLASSICS")).toBeInTheDocument();
    });
});
