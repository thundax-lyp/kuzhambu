import { KuzhambuAlert } from "@/components";

interface ClassicsPublicationErrorItem {
    lifecycleStatus?: string | null;
}

interface ClassicsPublicationErrorAlertProps {
    items: ClassicsPublicationErrorItem[];
}

export const ClassicsPublicationErrorAlert = ({ items }: ClassicsPublicationErrorAlertProps) => {
    const errorCount = items.filter((item) => item.lifecycleStatus === "ERROR").length;
    if (!errorCount) {
        return null;
    }

    return (
        <KuzhambuAlert
            showIcon
            type="warning"
            style={{ marginBottom: 12 }}
            title={`当前列表有 ${errorCount} 条稿件发布异常`}
            description="稿件可能仍有搜索索引或知识库外部残留；后台会继续独立清理。请查看发布任务详情确认最终一致性状态。"
        />
    );
};
