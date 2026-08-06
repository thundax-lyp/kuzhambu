import { useMutation } from "@tanstack/react-query";
import { Progress, Typography } from "antd";
import { KuzhambuButton, KuzhambuCard, KuzhambuSpace } from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import * as service from "@/pages/discovery/search-statistic/search-statistic-service";

const { Text } = Typography;

export const SearchIndexRebuildPanel = () => {
    const confirm = useKuzhambuConfirm();
    const rebuildMutation = useMutation({ mutationFn: service.rebuildSearchIndex });
    let statusText = "尚未触发重建。";
    let progressPercent = 0;
    let progressStatus: "active" | "exception" | "normal" | "success" = "normal";

    if (rebuildMutation.isPending) {
        statusText = "重建触发中，请等待任务完成。";
        progressPercent = 50;
        progressStatus = "active";
    } else if (rebuildMutation.isError) {
        statusText =
            rebuildMutation.error instanceof Error
                ? `重建触发失败：${rebuildMutation.error.message}`
                : "重建触发失败。";
        progressStatus = "exception";
    } else if (rebuildMutation.data !== undefined) {
        statusText = `重建结果：${rebuildMutation.data}`;
        progressPercent = 100;
        progressStatus = "success";
    }
    const shouldShowProgress =
        rebuildMutation.isPending || rebuildMutation.isError || rebuildMutation.data !== undefined;

    const confirmRebuild = () => {
        confirm.danger({
            title: "全量重建检索索引",
            message: "确认触发全量索引重建？",
            description: "重建期间检索数据可能发生变化，请确认当前适合执行该运维操作。",
            okText: "触发重建",
            onConfirm: () => rebuildMutation.mutateAsync({ confirm: true })
        });
    };

    return (
        <KuzhambuCard size="small">
            <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                <KuzhambuButton
                    testId="discovery-search-statistics-search-statistics-trigger-rebuild-button"
                    danger
                    loading={rebuildMutation.isPending}
                    onClick={confirmRebuild}
                >
                    触发重建
                </KuzhambuButton>
                <Text className="search-statistics-rebuild-status" type="secondary">
                    {statusText}
                </Text>
                {shouldShowProgress ? (
                    <Progress
                        aria-label="索引重建进度"
                        percent={progressPercent}
                        showInfo={false}
                        status={progressStatus}
                    />
                ) : null}
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
