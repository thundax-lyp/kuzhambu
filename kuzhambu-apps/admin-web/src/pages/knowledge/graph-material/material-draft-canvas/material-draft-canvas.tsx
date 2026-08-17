import { Descriptions, Empty, Typography } from "antd";
import { useMemo, useState } from "react";
import {
    KuzhambuButton,
    KuzhambuCard,
    KuzhambuGraph,
    KuzhambuSpace,
    KuzhambuTag
} from "@/components";
import type {
    GraphMaterialDetailRecord,
    GraphMaterialDraftObject,
    GraphMaterialRecord
} from "@/pages/knowledge/graph-material/graph-material-types";

const { Text } = Typography;

interface MaterialDraftCanvasProps {
    canApplyGraph: boolean;
    canEditGraph: boolean;
    detail: GraphMaterialDetailRecord | null;
    material: GraphMaterialRecord;
}

const toDraftObjects = (detail: GraphMaterialDetailRecord | null): GraphMaterialDraftObject[] =>
    (detail?.nodes ?? []).map((node) => ({
        id: node.id,
        name: node.name,
        sourceText: String(node.properties.evidence ?? node.source),
        type: node.nodeType
    }));

export const MaterialDraftCanvas = ({
    canApplyGraph,
    canEditGraph,
    detail,
    material
}: MaterialDraftCanvasProps) => {
    const [selectedDraftObject, setSelectedDraftObject] = useState<{
        materialId: string;
        objectId: string;
    } | null>(null);
    const isDraft = material.status === "DRAFT";
    const isPublished = material.status === "PUBLISHED";
    const canMutateDraft = isDraft && canEditGraph;
    const draftObjects = useMemo(() => toDraftObjects(detail), [detail]);
    const spoList = useMemo(
        () =>
            (detail?.edges ?? []).map((edge) => {
                const sourceNode = detail?.nodes.find((node) => node.id === edge.sourceNodeId);
                const targetNode = detail?.nodes.find((node) => node.id === edge.targetNodeId);
                return {
                    object: targetNode?.name ?? edge.targetNodeId,
                    predicate: edge.relationType,
                    subject: sourceNode?.name ?? edge.sourceNodeId
                };
            }),
        [detail]
    );
    const activeDraftObject =
        selectedDraftObject?.materialId === material.id
            ? (draftObjects.find((object) => object.id === selectedDraftObject.objectId) ?? null)
            : null;

    return (
        <KuzhambuCard
            title={`草稿图谱：${material.title}`}
            extra={
                <KuzhambuSpace>
                    {isPublished ? <KuzhambuTag type="success">只读</KuzhambuTag> : null}
                    <KuzhambuTag>{material.status}</KuzhambuTag>
                </KuzhambuSpace>
            }
        >
            <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                <Text type="secondary">
                    草稿图谱仅在素材详情抽屉的草稿图谱段展示。已发布素材保留查看能力，不开放草稿编辑动作。
                </Text>
                {spoList.length > 0 ? (
                    <KuzhambuGraph height={300} spoList={spoList} />
                ) : (
                    <Empty
                        data-testid="knowledge-graph-material-draft-graph-empty"
                        description="暂无草稿关系"
                    />
                )}
                {draftObjects.length > 0 ? (
                    <KuzhambuSpace wrap>
                        {draftObjects.map((object) => (
                            <KuzhambuButton
                                key={object.id}
                                testId={`knowledge-graph-material-open-object-${object.id}-button`}
                                onClick={() =>
                                    setSelectedDraftObject({
                                        materialId: material.id,
                                        objectId: object.id
                                    })
                                }
                            >
                                对象：{object.name}
                            </KuzhambuButton>
                        ))}
                    </KuzhambuSpace>
                ) : (
                    <Empty
                        data-testid="knowledge-graph-material-draft-object-empty"
                        description="暂无草稿对象"
                    />
                )}
                {activeDraftObject ? (
                    <Descriptions
                        bordered
                        column={1}
                        data-testid="knowledge-graph-material-draft-object-detail"
                        size="small"
                    >
                        <Descriptions.Item label="对象名称">
                            {activeDraftObject.name}
                        </Descriptions.Item>
                        <Descriptions.Item label="对象类型">
                            {activeDraftObject.type}
                        </Descriptions.Item>
                        <Descriptions.Item label="证据摘录">
                            {activeDraftObject.sourceText}
                        </Descriptions.Item>
                    </Descriptions>
                ) : null}
                {canMutateDraft ? (
                    <KuzhambuSpace wrap>
                        <KuzhambuButton testId="knowledge-graph-material-create-draft-object-button">
                            新增对象
                        </KuzhambuButton>
                        <KuzhambuButton testId="knowledge-graph-material-extract-draft-button">
                            抽取草稿
                        </KuzhambuButton>
                        <KuzhambuButton testId="knowledge-graph-material-import-draft-button">
                            导入草稿
                        </KuzhambuButton>
                    </KuzhambuSpace>
                ) : null}
                {!canApplyGraph && isPublished ? (
                    <Text type="secondary">当前账号仅可查看。</Text>
                ) : null}
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
