import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { GraphMaterialPageQuery } from "../graph-material-service";
import { MaterialFilters } from "./material-filters";

const renderFilters = (value: GraphMaterialPageQuery = { pageNo: 2, pageSize: 10 }) => {
    const onChange = vi.fn();
    render(<MaterialFilters totalCount={50} value={value} onChange={onChange} />);
    return onChange;
};

describe("MaterialFilters", () => {
    afterEach(() => {
        cleanup();
    });

    it("submits normalized filter values from the form", async () => {
        const onChange = renderFilters({
            contentType: "SANCAI_ENTRY",
            pageNo: 2,
            pageSize: 10,
            status: "PUBLISHED",
            taskDisposition: "ADOPTED_MERGE",
            taskExecutionStatus: "SUCCEEDED"
        });
        const user = userEvent.setup();

        await user.type(screen.getByLabelText("关键字"), "  人物  ");
        await user.type(screen.getByLabelText("分类"), "  person  ");
        await user.type(screen.getByLabelText("卷目"), "  volume-2  ");
        await user.click(screen.getByTestId("knowledge-graph-material-filter-submit-button"));

        expect(onChange).toHaveBeenCalledWith({
            categoryCode: "person",
            contentType: "SANCAI_ENTRY",
            keyword: "人物",
            pageNo: 1,
            pageSize: 10,
            status: "PUBLISHED",
            taskDisposition: "ADOPTED_MERGE",
            taskExecutionStatus: "SUCCEEDED",
            volumeCode: "volume-2"
        });
    });

    it("emits page and page size changes as query values", async () => {
        const onChange = renderFilters({ keyword: "人物", pageNo: 1, pageSize: 10 });
        const user = userEvent.setup();

        await user.click(screen.getByRole("listitem", { name: "2" }));

        expect(onChange).toHaveBeenCalledWith({
            keyword: "人物",
            pageNo: 2,
            pageSize: 10
        });
    });
});
