import { Descriptions, Empty, List, Tag, Typography } from "antd";
import type {
    LineageNodeRecord,
    LineageRelationRecord,
    LineageSourceRefRecord
} from "../lineage-types";

interface LineageDetailPanelProps {
    node?: LineageNodeRecord | null;
    relation?: LineageRelationRecord | null;
}

const { Text } = Typography;

const formatTimestamp = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("zh-CN", { hour12: false });
};

const renderSourceRefs = (sourceRefs: LineageSourceRefRecord[] = []) => {
    if (sourceRefs.length === 0) {
        return "-";
    }
    return (
        <List
            className="knowledge-lineage-detail-panel__sources"
            dataSource={sourceRefs}
            renderItem={(sourceRef) => (
                <List.Item>
                    <List.Item.Meta
                        title={sourceRef.sourceTitle || sourceRef.sourceContentType || "来源"}
                        description={
                            <div className="knowledge-lineage-detail-panel__source-description">
                                <Text type="secondary">
                                    {sourceRef.sourceContentType || "-"} /{" "}
                                    {sourceRef.sourceContentId ?? "-"}
                                </Text>
                                {sourceRef.snippet ? <Text>{sourceRef.snippet}</Text> : null}
                                {sourceRef.href ? (
                                    <a href={sourceRef.href} target="_blank" rel="noreferrer">
                                        打开来源
                                    </a>
                                ) : null}
                            </div>
                        }
                    />
                </List.Item>
            )}
        />
    );
};

const renderStatus = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    return <Tag>{value}</Tag>;
};

export const LineageDetailPanel = ({ node, relation }: LineageDetailPanelProps) => {
    if (relation) {
        return (
            <div className="knowledge-lineage-detail-panel">
                <Descriptions title="关系详情" column={1} bordered size="small">
                    <Descriptions.Item label="关系号">{relation.relationId}</Descriptions.Item>
                    <Descriptions.Item label="起点">
                        {relation.sourceNodeName || relation.sourceNodeId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="终点">
                        {relation.targetNodeName || relation.targetNodeId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="关系类型">
                        {relation.relationType || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="关系标签">
                        {relation.relationLabel || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="确认状态">
                        {renderStatus(relation.confirmationStatus)}
                    </Descriptions.Item>
                    <Descriptions.Item label="置信度">
                        {relation.confidence ?? "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="首次抽取">
                        {formatTimestamp(relation.firstExtractedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="最近抽取">
                        {formatTimestamp(relation.lastExtractedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="来源引用">
                        {renderSourceRefs(relation.sourceRefs)}
                    </Descriptions.Item>
                    <Descriptions.Item label="原始来源">
                        {relation.sourceRefsJson || "-"}
                    </Descriptions.Item>
                </Descriptions>
            </div>
        );
    }

    if (node) {
        return (
            <div className="knowledge-lineage-detail-panel">
                <Descriptions title="节点详情" column={1} bordered size="small">
                    <Descriptions.Item label="节点号">{node.nodeId}</Descriptions.Item>
                    <Descriptions.Item label="业务键">{node.nodeKey || "-"}</Descriptions.Item>
                    <Descriptions.Item label="名称">{node.name || "-"}</Descriptions.Item>
                    <Descriptions.Item label="节点类型">{node.nodeType || "-"}</Descriptions.Item>
                    <Descriptions.Item label="代际">{node.generation ?? "-"}</Descriptions.Item>
                    <Descriptions.Item label="性别">{node.gender || "-"}</Descriptions.Item>
                    <Descriptions.Item label="确认状态">
                        {renderStatus(node.confirmationStatus)}
                    </Descriptions.Item>
                    <Descriptions.Item label="置信度">{node.confidence ?? "-"}</Descriptions.Item>
                    <Descriptions.Item label="首次抽取">
                        {formatTimestamp(node.firstExtractedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="最近抽取">
                        {formatTimestamp(node.lastExtractedAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="来源引用">
                        {renderSourceRefs(node.sourceRefs)}
                    </Descriptions.Item>
                    <Descriptions.Item label="原始来源">
                        {node.sourceRefsJson || "-"}
                    </Descriptions.Item>
                </Descriptions>
            </div>
        );
    }

    return <Empty description="尚未选中节点或关系" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
};
