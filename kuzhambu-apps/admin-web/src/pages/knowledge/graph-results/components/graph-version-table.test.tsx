import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { GraphVersionTable } from "./graph-version-table";

describe("GraphVersionTable", () => {
    it("links graph versions to refinement workbench", () => {
        render(
            <GraphVersionTable
                versions={[
                    {
                        versionId: 71,
                        taskType: "GRAPH",
                        status: "APPLIED",
                        sourceContentType: "SANCAI_ENTRY",
                        sourceContentId: 1001,
                        versionNo: 2,
                        refinementApplied: false
                    }
                ]}
                onOpenDetail={vi.fn()}
                onOpenResults={vi.fn()}
            />
        );

        expect(screen.getByRole("link", { name: "进入精修" })).toHaveAttribute(
            "href",
            "/knowledge/refinement?graphVersionId=71"
        );
    });
});
