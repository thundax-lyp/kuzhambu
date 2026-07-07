import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { AiRefinementStreamPanel } from "./ai-refinement-stream-panel";

describe("AiRefinementStreamPanel", () => {
    it("renders stream deltas, failure reason, and close operation", async () => {
        const onClose = vi.fn();
        const user = userEvent.setup();

        render(
            <AiRefinementStreamPanel
                events={[
                    {
                        eventType: "delta",
                        deltaText: "第一段"
                    },
                    {
                        eventType: "delta",
                        deltaText: "第二段"
                    },
                    {
                        eventType: "error",
                        failureStage: "WORKER_STREAM",
                        errorType: "WORKER_PROTOCOL_FAILURE",
                        errorMessage: "Worker stream ended without completed event"
                    }
                ]}
                isStreaming={false}
                task={{
                    taskId: 7001,
                    status: "FAILED",
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001,
                    failureStage: "WORKER_STREAM",
                    errorType: "WORKER_PROTOCOL_FAILURE",
                    errorMessage: "Worker stream ended without completed event"
                }}
                onClose={onClose}
            />
        );

        expect(screen.getByLabelText("三才图会 AI 流式过程")).toBeInTheDocument();
        expect(screen.getByText("AI 流式过程")).toBeInTheDocument();
        expect(screen.getByText("第一段第二段")).toBeInTheDocument();
        expect(screen.getByText("失败原因")).toBeInTheDocument();
        expect(
            screen.getByText(
                "WORKER_STREAM / WORKER_PROTOCOL_FAILURE / Worker stream ended without completed event"
            )
        ).toBeInTheDocument();

        await user.click(screen.getByRole("button", { name: "关闭过程" }));

        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it("allows retry after stream error before task snapshot refreshes to failed", async () => {
        const onClose = vi.fn();
        const onRetry = vi.fn();
        const user = userEvent.setup();

        render(
            <AiRefinementStreamPanel
                events={[
                    {
                        eventType: "error",
                        failureStage: "WORKER_STREAM",
                        errorType: "WORKER_PROTOCOL_FAILURE",
                        errorMessage: "bad stream"
                    }
                ]}
                isStreaming={false}
                task={{
                    taskId: 7001,
                    status: "RUNNING",
                    capability: "image_analysis",
                    contentType: "SANCAI_ENTRY",
                    contentId: 3001
                }}
                onClose={onClose}
                onRetry={onRetry}
            />
        );

        await user.click(screen.getByRole("button", { name: /重\s*试/ }));

        expect(onRetry).toHaveBeenCalledTimes(1);
    });
});
