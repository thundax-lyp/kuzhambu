import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { TagBatchReviewPanel } from "./tag-batch-review-panel";

const categories = [
    { id: "11", name: "礼学", status: "ENABLED" },
    { id: "12", name: "禁用分类", status: "DISABLED" }
];

const selectedTags = [
    { id: "1001", name: "礼制" },
    { id: "1002", name: "祭祀" }
];

const openSelectAndChoose = async (label: string, optionText: string) => {
    const select = await screen.findByRole("combobox", { name: label });
    fireEvent.mouseDown(select);
    const options = await screen.findAllByText(optionText);
    await userEvent.click(options.at(-1)!);
};

describe("TagBatchReviewPanel", () => {
    afterEach(() => {
        cleanup();
    });

    it("requires category before approving selected tags", async () => {
        const onSubmit = vi.fn();
        render(
            <TagBatchReviewPanel
                categories={categories}
                decision="APPROVE"
                open
                reviewing={false}
                selectedTags={selectedTags}
                onClose={vi.fn()}
                onSubmit={onSubmit}
            />
        );

        expect(screen.getByRole("button", { name: "确认通过" })).toBeDisabled();

        await openSelectAndChoose("批量审核正式分类", "礼学");
        await userEvent.type(screen.getByLabelText("批量审核备注"), "确认进入正式标签");
        await userEvent.click(screen.getByRole("button", { name: "确认通过" }));

        expect(onSubmit).toHaveBeenCalledWith({
            tagIds: ["1001", "1002"],
            decision: "APPROVE",
            categoryId: "11",
            reviewNote: "确认进入正式标签"
        });
    });

    it("submits rejection without category", async () => {
        const onSubmit = vi.fn();
        render(
            <TagBatchReviewPanel
                categories={categories}
                decision="REJECT"
                open
                reviewing={false}
                selectedTags={selectedTags}
                onClose={vi.fn()}
                onSubmit={onSubmit}
            />
        );

        await userEvent.type(screen.getByLabelText("批量审核备注"), "不符合标签规范");
        await userEvent.click(screen.getByRole("button", { name: "确认拒绝" }));

        expect(onSubmit).toHaveBeenCalledWith({
            tagIds: ["1001", "1002"],
            decision: "REJECT",
            categoryId: undefined,
            reviewNote: "不符合标签规范"
        });
    });
});
