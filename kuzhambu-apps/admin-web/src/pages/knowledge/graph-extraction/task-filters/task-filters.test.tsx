import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { TaskFilters } from "./task-filters";
import type { GraphExtractionTaskPageQuery } from "../graph-extraction-service";

const renderFilters = (query: GraphExtractionTaskPageQuery = {}) => {
    const onChange = vi.fn();
    render(
        <TaskFilters
            query={{
                groupBy: "NONE",
                pageNo: 1,
                pageSize: 20,
                ...query
            }}
            total={45}
            onChange={onChange}
        />
    );
    return onChange;
};

describe("TaskFilters", () => {
    it("submits batchId filter in the task page query", async () => {
        const onChange = renderFilters();

        fireEvent.change(screen.getByLabelText("批次号"), {
            target: { value: " batch-001 " }
        });
        fireEvent.click(screen.getByTestId("knowledge-graph-extraction-task-filter-submit-button"));

        await waitFor(() => {
            expect(onChange).toHaveBeenCalledWith({
                batchId: "batch-001",
                groupBy: "NONE",
                pageNo: 1,
                pageSize: 20
            });
        });
    });

    it("submits contentRefs filter as content type and id records", async () => {
        const onChange = renderFilters();

        fireEvent.change(screen.getByLabelText("素材引用"), {
            target: { value: "SANCAI_ENTRY:1001, WANGQI_DOCUMENT:2002" }
        });
        fireEvent.click(screen.getByTestId("knowledge-graph-extraction-task-filter-submit-button"));

        await waitFor(() => {
            expect(onChange).toHaveBeenCalledWith({
                contentRefs: [
                    {
                        contentRefId: "1001",
                        contentType: "SANCAI_ENTRY"
                    },
                    {
                        contentRefId: "2002",
                        contentType: "WANGQI_DOCUMENT"
                    }
                ],
                groupBy: "NONE",
                pageNo: 1,
                pageSize: 20
            });
        });
    });

    it("keeps current filters when pagination changes", async () => {
        const onChange = renderFilters({
            batchId: "batch-001",
            contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }]
        });

        fireEvent.click(screen.getByTitle("2"));

        await waitFor(() => {
            expect(onChange).toHaveBeenCalledWith({
                batchId: "batch-001",
                contentRefs: [{ contentRefId: "1001", contentType: "SANCAI_ENTRY" }],
                groupBy: "NONE",
                pageNo: 2,
                pageSize: 20
            });
        });
    });
});
