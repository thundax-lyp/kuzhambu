import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { TagBatchMergePanel } from "./tag-batch-merge-panel";

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

describe("TagBatchMergePanel", () => {
    afterEach(() => {
        cleanup();
    });

    it("previews and applies batch merge actions with selected source tags", async () => {
        const onPreview = vi.fn();
        const onApply = vi.fn();

        render(
            <TagBatchMergePanel
                applying={false}
                candidateTargetTags={[{ id: "1002", name: "祭祀" }]}
                open
                preview={{
                    sourceTags: [
                        { id: "1001", name: "礼制" },
                        { id: "1003", name: "礼典" }
                    ],
                    targetTag: { id: "1002", name: "祭祀" },
                    aliasesToMerge: [{ id: "2001", name: "礼法", source: "MANUAL" }],
                    impactedContentRefs: [
                        {
                            id: "3001",
                            contentTitle: "周礼",
                            contentType: "CLASSICS",
                            source: "MANUAL"
                        }
                    ],
                    pendingReviewCount: 0,
                    governedRecordCount: 2
                }}
                previewing={false}
                selectedSourceTagIds={["1001", "1003"]}
                selectedSourceTags={[
                    { id: "1001", name: "礼制" },
                    { id: "1003", name: "礼典" }
                ]}
                onApply={onApply}
                onClose={vi.fn()}
                onPreview={onPreview}
            />
        );

        await openSelectAndChoose("批量合并目标标签", "祭祀（1002）");
        await userEvent.click(screen.getByRole("button", { name: "预览影响" }));
        await userEvent.click(screen.getByRole("button", { name: "执行批量合并" }));

        expect(onPreview).toHaveBeenCalledWith({
            sourceTagIds: ["1001", "1003"],
            targetTagId: "1002"
        });
        expect(onApply).toHaveBeenCalledWith({
            sourceTagIds: ["1001", "1003"],
            targetTagId: "1002"
        });
        expect(screen.getByText("礼法")).toBeInTheDocument();
        expect(screen.getByText("周礼 · CLASSICS")).toBeInTheDocument();
    });
});
