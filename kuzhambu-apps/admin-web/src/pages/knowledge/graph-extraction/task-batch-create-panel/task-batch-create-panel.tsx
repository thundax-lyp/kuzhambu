import { DatabaseOutlined, FolderOutlined } from "@ant-design/icons";
import { useMutation } from "@tanstack/react-query";
import { App, Typography } from "antd";
import { KuzhambuButton, KuzhambuCard, KuzhambuSpace } from "@/components";
import * as service from "../graph-extraction-service";
import type {
    GraphBatchExtractionResultRecord,
    GraphContentRefRecord
} from "../graph-extraction-types";

const { Text } = Typography;

export interface TaskBatchCreatePanelProps {
    canCreate: boolean;
    contentRefs: GraphContentRefRecord[];
    volumeCode?: string;
    volumeTitle?: string | null;
    onCreated?: (result: GraphBatchExtractionResultRecord) => Promise<void> | void;
}

export const TaskBatchCreatePanel = ({
    canCreate,
    contentRefs,
    volumeCode,
    volumeTitle,
    onCreated
}: TaskBatchCreatePanelProps) => {
    const { message: messageApi } = App.useApp();
    const selectedContentCount = contentRefs.length;
    const batchCreateMutation = useMutation({
        mutationFn: (command: service.GraphExtractionBatchCreateCommand) =>
            service.createBatchExtraction(command),
        onSuccess: async (result) => {
            await onCreated?.(result);
            messageApi.success("批量图谱抽取任务已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "批量图谱抽取任务创建失败");
        }
    });

    const createSelectedContentBatch = () => {
        if (!selectedContentCount) {
            return;
        }
        batchCreateMutation.mutate({ contentRefs });
    };

    const createVolumeBatch = () => {
        if (!volumeCode) {
            return;
        }
        batchCreateMutation.mutate({ volumeCode });
    };

    return (
        <KuzhambuCard size="small" title="批量创建任务" styles={{ body: { paddingBlock: 12 } }}>
            <KuzhambuSpace size={12} wrap>
                <Text type="secondary">
                    {volumeTitle ? `当前卷目：${volumeTitle}` : "请选择卷目后创建批量任务"}
                </Text>
                <KuzhambuButton
                    testId="knowledge-graph-extraction-batch-create-selected-button"
                    disabled={!canCreate || selectedContentCount === 0}
                    icon={<DatabaseOutlined />}
                    loading={batchCreateMutation.isPending}
                    type="primary"
                    onClick={createSelectedContentBatch}
                >
                    已选素材({selectedContentCount})
                </KuzhambuButton>
                <KuzhambuButton
                    testId="knowledge-graph-extraction-batch-create-volume-button"
                    disabled={!canCreate || !volumeCode}
                    icon={<FolderOutlined />}
                    loading={batchCreateMutation.isPending}
                    onClick={createVolumeBatch}
                >
                    整卷创建
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
