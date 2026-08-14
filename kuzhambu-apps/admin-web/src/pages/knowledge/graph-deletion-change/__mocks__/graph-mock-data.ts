export const graphDeletionChangeMockData = {
    changes: [
        {
            id: "deletion-change-1",
            materialId: "material-published",
            affectedNodeCount: 4,
            affectedRelationCount: 6,
            decisions: ["PRESERVE_CONTRIBUTION", "WITHDRAW_ASSOCIATIONS"]
        }
    ],
    failureMessage: "删除变更预检失败，请稍后重试。"
} as const;
