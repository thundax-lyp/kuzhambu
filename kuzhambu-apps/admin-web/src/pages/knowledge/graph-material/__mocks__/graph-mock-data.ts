export const graphMaterialMockData = {
    materials: [
        { id: "material-draft", title: "唐诗选注", status: "DRAFT" },
        { id: "material-publishing", title: "全唐诗卷一", status: "PUBLISHING" },
        { id: "material-published", title: "诗品", status: "PUBLISHED" },
        { id: "material-withdrawing", title: "文心雕龙", status: "WITHDRAWING" },
        {
            id: "material-failed",
            title: "古诗源",
            status: "FAILED",
            failureReason: "抽取任务在校验实体名称时失败。"
        }
    ],
    publicationPreview: [
        { id: "preview-create", color: "green", status: "CREATE" },
        { id: "preview-link", color: "orange", status: "LINK" },
        { id: "preview-conflict", color: "red", status: "CONFLICT" },
        { id: "preview-published", color: "blue", status: "PUBLISHED" }
    ],
    batchPublicationResults: [
        { materialId: "material-draft", status: "PUBLISHED" },
        {
            materialId: "material-failed",
            status: "FAILED",
            failureReason: "发布预览存在未解决冲突。"
        }
    ]
} as const;
