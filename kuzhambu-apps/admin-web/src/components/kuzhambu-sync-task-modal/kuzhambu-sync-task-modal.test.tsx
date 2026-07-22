import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { KuzhambuSyncTaskModal } from "./kuzhambu-sync-task-modal";
import type { KuzhambuSyncTaskAdapter } from "./kuzhambu-sync-task-modal";

interface DemoTask {
    id: number;
    status: string;
}

const adapter: KuzhambuSyncTaskAdapter<DemoTask> = {
    getId: (task) => task.id,
    getPhase: (task) => (task.status === "SUCCEEDED" ? "result_ready" : "tracking"),
    getStatusLabel: (task) => task.status
};

describe("KuzhambuSyncTaskModal", () => {
    it("renders create status and triggers create", async () => {
        const user = userEvent.setup();
        const onCreate = vi.fn();

        render(
            <KuzhambuSyncTaskModal<DemoTask, string>
                open
                createText="启动"
                task={{ id: 1, status: "RUNNING" }}
                taskAdapter={adapter}
                testId="sync-task-modal-demo"
                title="任务"
                onCancel={vi.fn()}
                onCreate={onCreate}
                renderBody={() => "任务内容"}
            />
        );

        expect(screen.getByText("RUNNING")).toBeInTheDocument();

        await user.click(screen.getByTestId("sync-task-modal-demo-create-button"));

        expect(onCreate).toHaveBeenCalledTimes(1);
    });

    it("loads result and passes it to apply", async () => {
        const user = userEvent.setup();
        const onApply = vi.fn();

        render(
            <KuzhambuSyncTaskModal<DemoTask, string>
                open
                applyText="采用"
                createText="启动"
                fetchResult={async () => "候选结果"}
                task={{ id: 1, status: "SUCCEEDED" }}
                taskAdapter={adapter}
                testId="sync-task-modal-demo"
                title="任务"
                onApply={onApply}
                onCancel={vi.fn()}
                onCreate={vi.fn()}
                renderBody={({ result }) => <span>{result || "加载中"}</span>}
            />
        );

        expect(await screen.findByText("候选结果")).toBeInTheDocument();

        await user.click(screen.getByTestId("sync-task-modal-demo-apply-button"));

        expect(onApply).toHaveBeenCalledWith("候选结果");
    });
});
