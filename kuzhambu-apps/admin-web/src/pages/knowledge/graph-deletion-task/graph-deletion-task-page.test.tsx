import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it } from "vitest";
import { replacePermissions } from "@/auth/permission-storage";
import { GraphDeletionTaskPage } from "./graph-deletion-task-page";

describe("GraphDeletionTaskPage", () => {
    afterEach(() => {
        cleanup();
        replacePermissions([]);
    });

    it("shows failure detail and retry status change", async () => {
        replacePermissions(["knowledge:graph:view"]);
        render(<GraphDeletionTaskPage />);
        const user = userEvent.setup();
        expect(screen.getByText("删除关联时检测到并发发布，请重试。")).toBeInTheDocument();
        await user.click(screen.getByRole("button", { name: "查看详情" }));
        expect(screen.getByText("删除任务详情")).toBeInTheDocument();
        await user.click(screen.getByTestId("knowledge-graph-deletion-task-retry-button"));
        expect(screen.getAllByText("SUCCEEDED")).toHaveLength(2);
        expect(screen.queryByText("删除关联时检测到并发发布，请重试。")).not.toBeInTheDocument();
    });

    it("shows permission and empty states", async () => {
        replacePermissions([]);
        const { rerender } = render(<GraphDeletionTaskPage />);
        expect(screen.getByText("无权查看图谱删除任务")).toBeInTheDocument();
        replacePermissions(["knowledge:graph:view"]);
        rerender(<GraphDeletionTaskPage />);
        await userEvent.click(screen.getByRole("button", { name: "模拟空态" }));
        expect(screen.getByText("暂无删除任务")).toBeInTheDocument();
    });
});
