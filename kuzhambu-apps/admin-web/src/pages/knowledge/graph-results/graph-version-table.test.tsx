import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { GraphVersionTable } from "./graph-version-table";

describe("GraphVersionTable", () => {
    const versions = [
        {
            versionId: "71",
            taskType: "GRAPH",
            status: "APPLIED",
            sourceContentType: "SANCAI_ENTRY",
            sourceContentId: "1001",
            versionNo: 2,
            refinementApplied: false
        }
    ];

    it("links graph versions to refinement workbench", () => {
        render(
            <GraphVersionTable
                canOpenRefinement
                versions={versions}
                onOpenDetail={vi.fn()}
                onOpenResults={vi.fn()}
            />
        );

        expect(screen.getByRole("link", { name: "进入精修" })).toHaveAttribute(
            "href",
            "/knowledge/refinement?graphVersionId=71"
        );
    });

    it("hides refinement workbench action without edit permission", () => {
        render(
            <GraphVersionTable versions={versions} onOpenDetail={vi.fn()} onOpenResults={vi.fn()} />
        );

        expect(screen.queryByRole("link", { name: "进入精修" })).not.toBeInTheDocument();
    });
});
