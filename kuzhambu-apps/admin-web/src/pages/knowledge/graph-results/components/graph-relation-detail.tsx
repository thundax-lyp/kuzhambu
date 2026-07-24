import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components";
import type { GraphRelationRecord } from "../graph-results-types";

interface GraphRelationDetailProps {
    loading?: boolean;
    onClose: () => void;
    open: boolean;
    relation?: GraphRelationRecord | null;
}

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

export const GraphRelationDetail = ({
    loading = false,
    onClose,
    open,
    relation
}: GraphRelationDetailProps) => {
    return (
        <KuzhambuDrawer
            testId="knowledge-graph-results-graph-relation-detail-drawer"
            title="正式关系详情"
            open={open}
            size="middle"
            loading={loading}
            onClose={onClose}
        >
            <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="关系号">{relation?.relationId || "-"}</Descriptions.Item>
                <Descriptions.Item label="业务键">{relation?.relationKey || "-"}</Descriptions.Item>
                <Descriptions.Item label="源实体">{relation?.sourceName || "-"}</Descriptions.Item>
                <Descriptions.Item label="目标实体">
                    {relation?.targetName || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="关系类型">
                    {relation?.relationType || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="确认状态">
                    {relation?.confirmationStatus || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="证据">{relation?.evidence || "-"}</Descriptions.Item>
                <Descriptions.Item label="版本号">
                    {relation?.latestVersionId || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="来源引用">
                    {relation?.sourceRefsJson || "-"}
                </Descriptions.Item>
                <Descriptions.Item label="首次抽取">
                    {formatTimestamp(relation?.firstExtractedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="最近抽取">
                    {formatTimestamp(relation?.lastExtractedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="确认时间">
                    {formatTimestamp(relation?.confirmedAt)}
                </Descriptions.Item>
            </Descriptions>
        </KuzhambuDrawer>
    );
};
