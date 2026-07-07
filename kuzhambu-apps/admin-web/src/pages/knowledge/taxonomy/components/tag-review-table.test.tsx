import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { TagReviewTable } from "./tag-review-table";

const tags = [
    {
        id: "1001",
        name: "礼制",
        categoryId: "11",
        categoryName: "礼学",
        source: "AI_EXTRACTED",
        reviewStatus: "PENDING"
    },
    {
        id: "1002",
        name: "祭祀",
        categoryId: "11",
        categoryName: "礼学",
        source: "MANUAL",
        reviewStatus: "PENDING"
    }
];

describe("TagReviewTable", () => {
    afterEach(() => {
        cleanup();
    });

    const renderTable = (selectedRowKeys: string[] = []) => {
        const props = {
            loading: false,
            query: { pageNo: 1, pageSize: 20 },
            selectedRowKeys,
            tags,
            totalCount: tags.length,
            onBatchApprove: vi.fn(),
            onBatchReject: vi.fn(),
            onChange: vi.fn(),
            onOpenReview: vi.fn(),
            onRefresh: vi.fn(),
            onSelectedRowKeysChange: vi.fn()
        };

        render(<TagReviewTable {...props} />);
        return props;
    };

    it("enables batch review actions when rows are selected", async () => {
        const props = renderTable(["1001"]);

        await userEvent.click(screen.getByRole("button", { name: /批量通过/ }));
        await userEvent.click(screen.getByRole("button", { name: /批量拒绝/ }));

        expect(props.onBatchApprove).toHaveBeenCalledTimes(1);
        expect(props.onBatchReject).toHaveBeenCalledTimes(1);
    });

    it("updates selected review row keys from table selection", async () => {
        const props = renderTable();
        const rowCheckboxes = screen.getAllByRole("checkbox").slice(1);

        expect(screen.getByRole("button", { name: /批量通过/ })).toBeDisabled();
        expect(screen.getByRole("button", { name: /批量拒绝/ })).toBeDisabled();
        await userEvent.click(rowCheckboxes[0]);

        expect(props.onSelectedRowKeysChange).toHaveBeenCalledWith(["1001"]);
    });
});
