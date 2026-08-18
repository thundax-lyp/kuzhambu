import { useQuery } from "@tanstack/react-query";
import { Descriptions, Empty, Spin, Tabs } from "antd";
import { KuzhambuDrawer } from "@/components";
import * as service from "../graph-governance-service";
import type {
    GraphGovernanceNodeDetailRecord,
    GraphGovernanceObjectType,
    GraphGovernanceRelationDetailRecord
} from "../graph-governance-types";

interface GovernanceDetailDrawerProps {
    objectId?: string;
    objectType?: GraphGovernanceObjectType;
    onClose: () => void;
}

const readObjectTitle = (objectType?: GraphGovernanceObjectType) =>
    objectType === "EDGE" ? "关系详情" : "节点详情";

export const GovernanceDetailDrawer = ({
    objectId,
    objectType,
    onClose
}: GovernanceDetailDrawerProps) => {
    const isNode = objectType === "NODE";
    const detailQuery = useQuery<
        GraphGovernanceNodeDetailRecord | GraphGovernanceRelationDetailRecord
    >({
        enabled: Boolean(objectId && objectType),
        queryFn: () =>
            isNode
                ? service.getPublishedNode(objectId ?? "")
                : service.getPublishedRelation(objectId ?? ""),
        queryKey: ["knowledge", "graph-governance", objectType, objectId]
    });
    const detail = detailQuery.data;
    const nodeDetail = isNode ? (detail as GraphGovernanceNodeDetailRecord | undefined) : undefined;
    const relationDetail = !isNode
        ? (detail as GraphGovernanceRelationDetailRecord | undefined)
        : undefined;
    const object = nodeDetail?.node ?? relationDetail?.edge;
    const title = nodeDetail?.node.name ?? relationDetail?.edge.relationType;
    const properties = detail?.properties ?? [];
    const materials = detail?.materials ?? [];
    const operations = detail?.operations ?? [];

    return (
        <KuzhambuDrawer
            open={Boolean(objectId && objectType)}
            onClose={onClose}
            title={readObjectTitle(objectType)}
            size="large"
            testId="knowledge-graph-governance-detail-drawer"
        >
            {detailQuery.isLoading ? <Spin /> : null}
            {detailQuery.isError ? <Empty description="详情加载失败，请关闭后重试。" /> : null}
            {detail ? (
                <Tabs
                    items={[
                        {
                            children: (
                                <Descriptions bordered column={1} size="small">
                                    <Descriptions.Item label="对象">
                                        {title || "-"}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="类型">
                                        {objectType === "NODE"
                                            ? nodeDetail?.node.nodeType || "-"
                                            : relationDetail?.edge.relationType || "-"}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="来源">
                                        {object?.source || "-"}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="状态">
                                        {object?.status || "-"}
                                    </Descriptions.Item>
                                    {relationDetail ? (
                                        <>
                                            <Descriptions.Item label="来源节点">
                                                {relationDetail.sourceNode.name ||
                                                    relationDetail.sourceNode.id}
                                            </Descriptions.Item>
                                            <Descriptions.Item label="目标节点">
                                                {relationDetail.targetNode.name ||
                                                    relationDetail.targetNode.id}
                                            </Descriptions.Item>
                                        </>
                                    ) : null}
                                </Descriptions>
                            ),
                            key: "summary",
                            label: "对象"
                        },
                        {
                            children: properties.length ? (
                                <Descriptions bordered column={1} size="small">
                                    {properties.map((property) => (
                                        <Descriptions.Item
                                            key={property.id}
                                            label={property.propertyName}
                                        >
                                            {property.value}
                                            {property.preferred ? "（首选）" : ""}
                                        </Descriptions.Item>
                                    ))}
                                </Descriptions>
                            ) : (
                                <Empty
                                    description="暂无属性"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                />
                            ),
                            key: "properties",
                            label: `属性 ${properties.length}`
                        },
                        {
                            children: materials.length ? (
                                <Descriptions bordered column={1} size="small">
                                    {materials.map((material) => (
                                        <Descriptions.Item
                                            key={material.id}
                                            label={material.mappingType}
                                        >
                                            {`${material.contentRef.contentType} #${material.contentRef.contentRefId} · ${material.status}`}
                                        </Descriptions.Item>
                                    ))}
                                </Descriptions>
                            ) : (
                                <Empty
                                    description="暂无来源素材"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                />
                            ),
                            key: "sources",
                            label: `来源 ${materials.length}`
                        },
                        {
                            children: operations.length ? (
                                <Descriptions bordered column={1} size="small">
                                    {operations.map((operation) => (
                                        <Descriptions.Item
                                            key={operation.id}
                                            label={operation.operationType}
                                        >
                                            {operation.reason || "未填写原因"}
                                        </Descriptions.Item>
                                    ))}
                                </Descriptions>
                            ) : (
                                <Empty
                                    description="暂无治理记录"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                />
                            ),
                            key: "operations",
                            label: `审计 ${operations.length}`
                        }
                    ]}
                />
            ) : null}
        </KuzhambuDrawer>
    );
};
