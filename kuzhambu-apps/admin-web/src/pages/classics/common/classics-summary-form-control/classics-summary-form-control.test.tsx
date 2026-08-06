import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ClassicsSummaryFormControl } from "./classics-summary-form-control";

describe("ClassicsSummaryFormControl", () => {
    it("renders the shared summary input without the AI action while creating", () => {
        render(
            <ClassicsSummaryFormControl
                aiButtonTestId="summary-ai-button"
                ariaLabel="摘要"
                mode="create"
                onOpenAiSummary={vi.fn()}
            />
        );

        expect(screen.getByLabelText("摘要")).toBeInTheDocument();
        expect(screen.queryByRole("button", { name: "AI 摘要" })).not.toBeInTheDocument();
    });

    it("opens the AI summary action while editing", () => {
        const onOpenAiSummary = vi.fn();
        render(
            <ClassicsSummaryFormControl
                aiButtonTestId="summary-ai-button"
                ariaLabel="摘要"
                mode="edit"
                onOpenAiSummary={onOpenAiSummary}
            />
        );

        fireEvent.click(screen.getByRole("button", { name: "AI 摘要" }));
        expect(onOpenAiSummary).toHaveBeenCalledOnce();
    });
});
