export const graphDeletionTaskMockData = {
    tasks: [
        {
            id: "deletion-task-failed",
            materialId: "material-published",
            status: "FAILED",
            failureReason: "删除关联时检测到并发发布，请重试。"
        }
    ]
} as const;
