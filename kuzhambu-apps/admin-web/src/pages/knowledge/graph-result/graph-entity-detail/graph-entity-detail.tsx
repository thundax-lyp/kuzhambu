import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components";
import type { GraphEntityRecord } from "../graph-result-types";

interface GraphEntityDetailProps {
    entity?: GraphEntityRecord | null;
    loading?: boolean;
    onClose: () => void;
    open: boolean;
}

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

export const GraphEntityDetail = ({
    entity,
    loading = false,
    onClose,
    open
}: GraphEntityDetailProps) => {
    return (
        <KuzhambuDrawer
            testId="knowledge-graph-results-graph-entity-detail-drawer"
            title="正式实体详情"
            open={open}
            size="middle"
            loading={loading}
            onClose={onClose}
        >
            <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="实体号">{entity?.entityId || "-"}</Descriptions.Item>
                <Descriptions.Item label="业务键">{entity?.entityKey || "-"}</Descriptions.Item>
                <Descriptions.Item label="名称">{entity?.name || "-"}</Descriptions.Item>
                <Descriptions.Item label="类型">{entity?.entityType || "-"}</Descriptions.Item>
                <Descriptions.Item label="确认状态">
                    {entity?.confirmationStatus || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="版本号">
                    {entity?.latestVersionId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="来源引用">
                    {entity?.sourceRefsJson || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="描述">{entity?.description || "-"}</Descriptions.Item>
                <Descriptions.Item label="首次抽取">
                    {formatTimestamp(entity?.firstExtractedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="最近抽取">
                    {formatTimestamp(entity?.lastExtractedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="确认时间">
                    {formatTimestamp(entity?.confirmedAt)}
                </Descriptions.Item>
            </Descriptions>
        </KuzhambuDrawer>
    );
};
