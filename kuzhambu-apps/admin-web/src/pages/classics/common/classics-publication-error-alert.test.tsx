import { render, screen } from "@testing-library/react";
import { ClassicsPublicationErrorAlert } from "./classics-publication-error-alert";

describe("ClassicsPublicationErrorAlert", () => {
    it("only explains external residue when the current result contains errors", () => {
        const { rerender } = render(
            <ClassicsPublicationErrorAlert items={[{ lifecycleStatus: "DRAFT" }]} />
        );
        expect(screen.queryByText(/外部残留/)).not.toBeInTheDocument();

        rerender(
            <ClassicsPublicationErrorAlert
                items={[{ lifecycleStatus: "ERROR" }, { lifecycleStatus: "ERROR" }]}
            />
        );
        expect(screen.getByText("当前列表有 2 条稿件发布异常")).toBeInTheDocument();
        expect(screen.getByText(/后台会继续独立清理/)).toBeInTheDocument();
    });
});
