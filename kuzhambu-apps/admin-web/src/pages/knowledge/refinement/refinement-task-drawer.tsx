import { Col, Empty, Row, Typography } from "antd";
import { KuzhambuSegmentedDrawer, KuzhambuSpace, KuzhambuCard } from "@/components";
import type { KuzhambuSegmentedDrawerSection } from "@/components";

import { RefinementApplyResultPanel } from "./refinement-apply-result-panel";
import { RefinementEntityTable } from "./refinement-entity-table";
import { RefinementLineageNodeTable } from "./refinement-lineage-node-table";
import { RefinementLineageRelationTable } from "./refinement-lineage-relation-table";
import { RefinementProgressSummaryPanel } from "./refinement-progress-summary";
import { RefinementQualityAnnotationTable } from "./refinement-quality-annotation-table";
import { RefinementRelationTable } from "./refinement-relation-table";
import type { RefinementWorkbenchSection } from "./hooks/use-refinement-workbench";
import type {
    QualityAnnotationRecord,
    QualityAnnotationTarget,
    QualitySummaryRecord,
    RefinementApplyRecord,
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementRelationRecord
} from "./refinement-types";

const { Text } = Typography;

interface RefinementTaskDrawerProps {
    activeSection: RefinementWorkbenchSection;
    applyFollowUp: RefinementApplyRecord | null;
    applying?: boolean;
    canEdit?: boolean;
    detail: RefinementDetailRecord | null;
    detailEyebrow: string;
    qualityAnnotations: QualityAnnotationRecord[];
    qualityAnnotationsLoading?: boolean;
    qualitySummary?: QualitySummaryRecord | null;
    onAddEntity: () => void;
    onAddRelation: () => void;
    onAnnotate: (target: Omit<QualityAnnotationTarget, "graphVersionId">) => void;
    onApplyTask: () => void;
    onClose: () => void;
    onConfirmEntity: (entity: RefinementEntityRecord) => void;
    onConfirmRelation: (relation: RefinementRelationRecord) => void;
    onDeleteEntity: (entity: RefinementEntityRecord) => void;
    onDeleteRelation: (relation: RefinementRelationRecord) => void;
    onEditAnnotation: (annotation: QualityAnnotationRecord) => void;
    onEditEntity: (entity: RefinementEntityRecord) => void;
    onEditRelation: (relation: RefinementRelationRecord) => void;
    onSectionChange: (section: RefinementWorkbenchSection) => void;
}

const formatPercent = (value?: number | null) => `${((value || 0) * 100).toFixed(0)}%`;

export const RefinementTaskDrawer = ({
    activeSection,
    applyFollowUp,
    applying = false,
    canEdit = false,
    detail,
    detailEyebrow,
    qualityAnnotations,
    qualityAnnotationsLoading = false,
    qualitySummary,
    onAddEntity,
    onAddRelation,
    onAnnotate,
    onApplyTask,
    onClose,
    onConfirmEntity,
    onConfirmRelation,
    onDeleteEntity,
    onDeleteRelation,
    onEditAnnotation,
    onEditEntity,
    onEditRelation,
    onSectionChange
}: RefinementTaskDrawerProps) => {
    const sections: Array<KuzhambuSegmentedDrawerSection<RefinementWorkbenchSection>> = [
        {
            content: (
                <RefinementEntityTable
                    canEdit={canEdit}
                    entities={detail?.entities || []}
                    onAdd={onAddEntity}
                    onAnnotate={(entity) =>
                        onAnnotate({
                            objectType: "ENTITY",
                            objectKey: entity.entityKey || "",
                            sourceContentType: detail?.sourceContentType,
                            sourceContentId: detail?.sourceContentId
                        })
                    }
                    onConfirm={onConfirmEntity}
                    onDelete={onDeleteEntity}
                    onEdit={onEditEntity}
                />
            ),
            label: "实体",
            value: "entities"
        },
        {
            content: (
                <RefinementRelationTable
                    canEdit={canEdit}
                    relations={detail?.relations || []}
                    onAdd={onAddRelation}
                    onAnnotate={(relation) =>
                        onAnnotate({
                            objectType: "RELATION",
                            objectKey: relation.relationKey || "",
                            sourceContentType: detail?.sourceContentType,
                            sourceContentId: detail?.sourceContentId
                        })
                    }
                    onConfirm={onConfirmRelation}
                    onDelete={onDeleteRelation}
                    onEdit={onEditRelation}
                />
            ),
            label: "关系",
            value: "relations"
        },
        {
            content: (
                <RefinementLineageNodeTable
                    canEdit={canEdit}
                    nodes={detail?.lineageNodes || []}
                    sourceContentId={detail?.sourceContentId}
                    sourceContentType={detail?.sourceContentType}
                    onAnnotate={onAnnotate}
                />
            ),
            label: "世系节点",
            value: "lineageNodes"
        },
        {
            content: (
                <RefinementLineageRelationTable
                    canEdit={canEdit}
                    relations={detail?.lineageRelations || []}
                    sourceContentId={detail?.sourceContentId}
                    sourceContentType={detail?.sourceContentType}
                    onAnnotate={onAnnotate}
                />
            ),
            label: "世系关系",
            value: "lineageRelations"
        },
        {
            content: (
                <RefinementQualityAnnotationTable
                    annotations={qualityAnnotations}
                    loading={qualityAnnotationsLoading}
                    onEdit={onEditAnnotation}
                />
            ),
            label: "质量标注",
            value: "annotations"
        },
        {
            content: applyFollowUp ? (
                <RefinementApplyResultPanel applyResult={applyFollowUp} />
            ) : (
                <Empty description="应用任务后展示图谱结果、重生成和质量报告后续入口。" />
            ),
            label: "后续动作",
            value: "followUp"
        }
    ];

    return (
        <KuzhambuSegmentedDrawer<RefinementWorkbenchSection>
            activeSection={activeSection}
            destroyOnHidden
            footerActions={[
                {
                    action: onApplyTask,
                    disabled: !detail || !canEdit,
                    loading: applying,
                    testId: "knowledge-refinement-task-apply-button",
                    title: "应用任务",
                    type: "primary"
                }
            ]}
            open={detail !== null}
            sectionClassName="knowledge-refinement-task-drawer-section"
            sections={sections}
            size="large"
            testId="knowledge-refinement-task-drawer"
            title="任务详情"
            onClose={onClose}
            onSectionChange={onSectionChange}
        >
            <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                <div className="knowledge-refinement-task-drawer-meta">
                    <Text strong>{detailEyebrow}</Text>
                    <Text type="secondary">
                        来源 {detail?.sourceContentType || "-"} / #{detail?.sourceContentId || "-"}
                    </Text>
                </div>
                <Row gutter={[12, 12]}>
                    <Col xs={24} lg={12}>
                        <KuzhambuCard className="knowledge-refinement-card" title="精修进度">
                            <RefinementProgressSummaryPanel
                                summary={detail?.progressSummary ?? null}
                            />
                        </KuzhambuCard>
                    </Col>
                    <Col xs={24} lg={12}>
                        <KuzhambuCard className="knowledge-refinement-card" title="质量汇总">
                            <dl className="knowledge-refinement-quality-list">
                                <div>
                                    <dt>实体覆盖率</dt>
                                    <dd>{formatPercent(qualitySummary?.entityCoverageRate)}</dd>
                                </div>
                                <div>
                                    <dt>关系准确率</dt>
                                    <dd>{formatPercent(qualitySummary?.relationAccuracyRate)}</dd>
                                </div>
                                <div>
                                    <dt>完整度</dt>
                                    <dd>{formatPercent(qualitySummary?.completenessRate)}</dd>
                                </div>
                            </dl>
                        </KuzhambuCard>
                    </Col>
                </Row>
            </KuzhambuSpace>
        </KuzhambuSegmentedDrawer>
    );
};
