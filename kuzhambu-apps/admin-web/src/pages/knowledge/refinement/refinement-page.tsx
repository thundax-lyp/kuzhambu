import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Card, Col, Empty, Row, Table, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import type { ColumnsType } from "antd/es/table";
import { RefinementEntityDeleteModal } from "./components/refinement-entity-delete-modal";
import { RefinementEntityEditor } from "./components/refinement-entity-editor";
import { RefinementEntityTable } from "./components/refinement-entity-table";
import { RefinementFilterForm } from "./components/refinement-filter-form";
import { RefinementProgressSummaryPanel } from "./components/refinement-progress-summary";
import { RefinementQualityAnnotationDrawer } from "./components/refinement-quality-annotation-drawer";
import { RefinementQualityAnnotationTable } from "./components/refinement-quality-annotation-table";
import { RefinementRelationDeleteModal } from "./components/refinement-relation-delete-modal";
import { RefinementRelationEditor } from "./components/refinement-relation-editor";
import { RefinementRelationTable } from "./components/refinement-relation-table";
import { RefinementWorkbenchTable } from "./components/refinement-workbench-table";
import * as service from "./refinement-service";
import type {
    QualityAnnotationRecord,
    QualityAnnotationTarget,
    RefinementApplyRecord,
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementLineageNodeRecord,
    RefinementLineageRelationRecord,
    RefinementRelationRecord,
    RefinementTaskPageQuery
} from "./refinement-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./refinement-page.css";

const { Text, Title } = Typography;

const readDetailTaskId = (detail: RefinementDetailRecord | null) => detail?.refinementTaskId ?? 0;
const readStatusColor = (status?: string | null) =>
    status === "MANUAL_CONFIRMED" ? "green" : "blue";
const buildGraphResultsHref = (applyResult: RefinementApplyRecord) =>
    `/knowledge/graph-results?graphVersionId=${encodeURIComponent(String(applyResult.graphVersionId || ""))}`;
const buildGraphRegenerateHref = (applyResult: RefinementApplyRecord) => {
    const params = new URLSearchParams({
        regenerate: "1",
        taskType: applyResult.taskType || "",
        sourceTaskId: String(applyResult.sourceTaskId || ""),
        triggerSource: applyResult.triggerSource || "REFINEMENT_APPLIED",
        replaceUnconfirmedOnly: String(applyResult.replaceUnconfirmedOnly ?? true)
    });
    if (applyResult.selectionScopeJson) {
        params.set("selectionScopeJson", applyResult.selectionScopeJson);
    }
    return `/knowledge/graph-extraction?${params.toString()}`;
};
const buildQualityReportHref = (applyResult: RefinementApplyRecord) =>
    `/knowledge/quality-report?graphVersionId=${encodeURIComponent(
        String(applyResult.graphVersionId || "")
    )}&regenerate=1`;

export const RefinementPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canView = hasPermission("knowledge:refinement:view");
    const canEdit = hasPermission("knowledge:refinement:edit");
    const [taskQuery, setTaskQuery] = useState<RefinementTaskPageQuery>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [detail, setDetail] = useState<RefinementDetailRecord | null>(null);
    const [entityEditorOpen, setEntityEditorOpen] = useState(false);
    const [editingEntity, setEditingEntity] = useState<RefinementEntityRecord | null>(null);
    const [deletingEntity, setDeletingEntity] = useState<RefinementEntityRecord | null>(null);
    const [relationEditorOpen, setRelationEditorOpen] = useState(false);
    const [editingRelation, setEditingRelation] = useState<RefinementRelationRecord | null>(null);
    const [deletingRelation, setDeletingRelation] = useState<RefinementRelationRecord | null>(null);
    const [annotationTarget, setAnnotationTarget] = useState<QualityAnnotationTarget | null>(null);
    const [applyFollowUp, setApplyFollowUp] = useState<RefinementApplyRecord | null>(null);

    const taskPageQuery = useQuery({
        queryKey: ["knowledge", "refinement", "tasks", taskQuery],
        queryFn: () => service.pageTasks(taskQuery),
        enabled: canView || canEdit,
        retry: false
    });

    const qualitySummaryQuery = useQuery({
        queryKey: ["knowledge", "refinement", "quality-summary", readDetailTaskId(detail)],
        queryFn: () => service.getQualitySummary({ refinementTaskId: readDetailTaskId(detail) }),
        enabled: detail !== null,
        retry: false
    });

    const qualityAnnotationQuery = useQuery({
        queryKey: ["knowledge", "refinement", "annotations", readDetailTaskId(detail)],
        queryFn: () =>
            service.pageAnnotations({
                refinementTaskId: readDetailTaskId(detail),
                pageNo: 1,
                pageSize: 200
            }),
        enabled: detail !== null,
        retry: false
    });

    const refreshDetail = async (refinementTaskId: number) => {
        const nextDetail = await service.getTaskDetail({ refinementTaskId });
        setDetail(nextDetail);
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "quality-summary", refinementTaskId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "tasks"]
            }),
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "annotations", refinementTaskId]
            })
        ]);
        return nextDetail;
    };

    const openTaskMutation = useMutation({
        mutationFn: service.getTaskDraft,
        onSuccess: async (nextDetail) => {
            setDetail(nextDetail);
            setApplyFollowUp(null);
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "tasks"]
            });
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "打开精修任务失败");
        }
    });

    const applyTaskMutation = useMutation({
        mutationFn: service.applyTask,
        onSuccess: async (applyResult) => {
            setApplyFollowUp(applyResult);
            setDetail((current) =>
                current && current.refinementTaskId === applyResult.refinementTaskId
                    ? {
                          ...current,
                          status: applyResult.status,
                          graphVersionId: applyResult.graphVersionId ?? current.graphVersionId
                      }
                    : current
            );
            await queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "tasks"]
            });
            messageApi.success("精修任务已应用");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "应用精修任务失败");
        }
    });

    const entityMutation = useMutation({
        mutationFn: async (request: RefinementEntityRecord) => {
            const refinementTaskId = readDetailTaskId(detail);
            const operatorId = 1;
            if (request.entityKey && request.confirmationStatus === "MANUAL_CONFIRMED") {
                return service.confirmEntity({
                    refinementTaskId,
                    entityKey: request.entityKey,
                    operatorId
                });
            }
            const command = {
                refinementTaskId,
                entityId: request.entityId,
                entityKey: request.entityKey,
                name: request.name,
                entityType: request.entityType,
                description: request.description,
                sourceRefsJson: request.sourceRefsJson,
                sortOrder: request.sortOrder,
                operatorId
            };
            return request.entityKey ? service.updateEntity(command) : service.addEntity(command);
        },
        onSuccess: async () => {
            await refreshDetail(readDetailTaskId(detail));
            setEntityEditorOpen(false);
            setEditingEntity(null);
            messageApi.success("实体草稿已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "实体草稿保存失败");
        }
    });

    const entityDeleteMutation = useMutation({
        mutationFn: async (record: RefinementEntityRecord) =>
            service.deleteEntity({
                refinementTaskId: readDetailTaskId(detail),
                entityKey: record.entityKey || "",
                operatorId: 1
            }),
        onSuccess: async () => {
            await refreshDetail(readDetailTaskId(detail));
            setDeletingEntity(null);
            messageApi.success("实体草稿已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "实体草稿删除失败");
        }
    });

    const relationMutation = useMutation({
        mutationFn: async (request: RefinementRelationRecord) => {
            const refinementTaskId = readDetailTaskId(detail);
            const operatorId = 1;
            if (request.relationKey && request.confirmationStatus === "MANUAL_CONFIRMED") {
                return service.confirmRelation({
                    refinementTaskId,
                    relationKey: request.relationKey,
                    operatorId
                });
            }
            const command = {
                refinementTaskId,
                relationId: request.relationId,
                relationKey: request.relationKey,
                sourceEntityKey: request.sourceEntityKey,
                targetEntityKey: request.targetEntityKey,
                sourceName: request.sourceName,
                targetName: request.targetName,
                relationType: request.relationType,
                evidence: request.evidence,
                sourceRefsJson: request.sourceRefsJson,
                sortOrder: request.sortOrder,
                operatorId
            };
            return request.relationKey
                ? service.updateRelation(command)
                : service.addRelation(command);
        },
        onSuccess: async () => {
            await refreshDetail(readDetailTaskId(detail));
            setRelationEditorOpen(false);
            setEditingRelation(null);
            messageApi.success("关系草稿已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "关系草稿保存失败");
        }
    });

    const relationDeleteMutation = useMutation({
        mutationFn: async (record: RefinementRelationRecord) =>
            service.deleteRelation({
                refinementTaskId: readDetailTaskId(detail),
                relationKey: record.relationKey || "",
                operatorId: 1
            }),
        onSuccess: async () => {
            await refreshDetail(readDetailTaskId(detail));
            setDeletingRelation(null);
            messageApi.success("关系草稿已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "关系草稿删除失败");
        }
    });

    const annotationMutation = useMutation({
        mutationFn: async (
            request: Pick<
                QualityAnnotationRecord,
                "annotationStatus" | "annotationLabel" | "comment"
            >
        ) => {
            if (!annotationTarget) {
                throw new Error("未选择标注对象");
            }
            const existingAnnotation = qualityAnnotationQuery.data?.records?.find(
                (annotation) =>
                    annotation.objectType === annotationTarget.objectType &&
                    annotation.objectKey === annotationTarget.objectKey
            );
            return service.updateAnnotation({
                annotationId: existingAnnotation?.annotationId,
                ...annotationTarget,
                ...request,
                operatorId: 1
            });
        },
        onSuccess: async () => {
            await refreshDetail(readDetailTaskId(detail));
            setAnnotationTarget(null);
            messageApi.success("质量标注已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "质量标注保存失败");
        }
    });

    const annotationDeleteMutation = useMutation({
        mutationFn: async (annotation: QualityAnnotationRecord) =>
            service.deleteAnnotation({
                annotationId: annotation.annotationId,
                operatorId: 1
            }),
        onSuccess: async () => {
            await refreshDetail(readDetailTaskId(detail));
            setAnnotationTarget(null);
            messageApi.success("质量标注已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "质量标注删除失败");
        }
    });

    const progressSummary = detail?.progressSummary ?? null;
    const qualitySummary = qualitySummaryQuery.data;
    const qualityAnnotations = useMemo(
        () => qualityAnnotationQuery.data?.records || [],
        [qualityAnnotationQuery.data?.records]
    );
    const editingAnnotation = useMemo(() => {
        if (!annotationTarget) {
            return null;
        }
        return (
            qualityAnnotations.find(
                (annotation) =>
                    annotation.objectType === annotationTarget.objectType &&
                    annotation.objectKey === annotationTarget.objectKey
            ) || null
        );
    }, [annotationTarget, qualityAnnotations]);
    const taskItems = taskPageQuery.data?.records || [];
    const detailReady = detail !== null;
    const detailEyebrow = useMemo(() => {
        if (!detail) {
            return "先从左侧任务列表打开一条精修任务";
        }
        return `${detail.taskType || "GRAPH"} / ${detail.sourceCategoryName || detail.sourceCategoryCode || "-"}`;
    }, [detail]);
    const openAnnotation = (target: Omit<QualityAnnotationTarget, "graphVersionId">) => {
        if (!target.objectKey) {
            messageApi.warning("当前对象缺少稳定键，无法标注");
            return;
        }
        setAnnotationTarget({
            ...target,
            graphVersionId: detail?.graphVersionId
        });
    };
    const openAnnotationFromRecord = (annotation: QualityAnnotationRecord) => {
        if (!annotation.objectType || !annotation.objectKey) {
            messageApi.warning("当前标注缺少对象信息，无法编辑");
            return;
        }
        setAnnotationTarget({
            objectType: annotation.objectType,
            objectKey: annotation.objectKey,
            sourceContentType: detail?.sourceContentType,
            sourceContentId: detail?.sourceContentId,
            graphVersionId: annotation.graphVersionId ?? detail?.graphVersionId
        });
    };
    const lineageNodeColumns: ColumnsType<RefinementLineageNodeRecord> = [
        { title: "名称", dataIndex: "name", key: "name" },
        { title: "类型", dataIndex: "nodeType", key: "nodeType" },
        { title: "代际", dataIndex: "generation", key: "generation" },
        {
            title: "确认状态",
            dataIndex: "confirmationStatus",
            key: "confirmationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        { title: "操作类型", dataIndex: "operationType", key: "operationType" },
        {
            key: "actions",
            render: (_, node) => (
                <KuzhambuButton
                    name="标注"
                    disabled={!canEdit}
                    onClick={() =>
                        openAnnotation({
                            objectType: "LINEAGE_NODE",
                            objectKey: node.nodeKey || "",
                            sourceContentType: detail?.sourceContentType,
                            sourceContentId: detail?.sourceContentId
                        })
                    }
                >
                    标注
                </KuzhambuButton>
            )
        }
    ];
    const lineageRelationColumns: ColumnsType<RefinementLineageRelationRecord> = [
        { title: "源名称", dataIndex: "sourceName", key: "sourceName" },
        { title: "目标名称", dataIndex: "targetName", key: "targetName" },
        { title: "关系类型", dataIndex: "relationType", key: "relationType" },
        {
            title: "确认状态",
            dataIndex: "confirmationStatus",
            key: "confirmationStatus",
            render: (status?: string | null) => (
                <Tag color={readStatusColor(status)}>{status || "-"}</Tag>
            )
        },
        { title: "操作类型", dataIndex: "operationType", key: "operationType" },
        {
            key: "actions",
            render: (_, relation) => (
                <KuzhambuButton
                    name="标注"
                    disabled={!canEdit}
                    onClick={() =>
                        openAnnotation({
                            objectType: "LINEAGE_RELATION",
                            objectKey: relation.relationKey || "",
                            sourceContentType: detail?.sourceContentType,
                            sourceContentId: detail?.sourceContentId
                        })
                    }
                >
                    标注
                </KuzhambuButton>
            )
        }
    ];

    return (
        <KuzhambuPage
            className="knowledge-refinement-page refinement-page"
            description="围绕待精修任务完成实体、关系确认和应用回正式事实。"
            eyebrow="Knowledge / Refinement"
            title="知识图谱精修工作台"
        >
            <KuzhambuSpace className="knowledge-refinement-layout" orientation="vertical" size={16}>
                <Alert
                    banner
                    title="本页支持打开精修任务、修订实体关系草稿、标注质量问题并应用回正式事实。"
                    type="info"
                />

                <section aria-labelledby="knowledge-refinement-task-section">
                    <div className="knowledge-refinement-section-header">
                        <Title id="knowledge-refinement-task-section" level={4}>
                            待精修任务
                        </Title>
                        <Text type="secondary">按门类、来源和状态筛选后打开任务进入精修。</Text>
                    </div>
                    <Card className="knowledge-refinement-card" variant="borderless">
                        <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                            <RefinementFilterForm
                                loading={taskPageQuery.isLoading}
                                value={taskQuery}
                                onChange={setTaskQuery}
                            />
                            <RefinementWorkbenchTable
                                items={taskItems}
                                loading={taskPageQuery.isLoading}
                                onOpenTask={(item) =>
                                    openTaskMutation.mutate({
                                        graphVersionId: item.graphVersionId || 0,
                                        openedBy: 1
                                    })
                                }
                            />
                        </KuzhambuSpace>
                    </Card>
                </section>

                <section aria-labelledby="knowledge-refinement-detail-section">
                    <div className="knowledge-refinement-section-header">
                        <Title id="knowledge-refinement-detail-section" level={4}>
                            任务详情
                        </Title>
                        <Text type="secondary">{detailEyebrow}</Text>
                    </div>
                    {detailReady ? (
                        <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                            {applyFollowUp ? (
                                <Alert
                                    action={
                                        <KuzhambuSpace size={8}>
                                            <KuzhambuButton
                                                name="查看图谱结果"
                                                href={buildGraphResultsHref(applyFollowUp)}
                                                size="small"
                                            >
                                                查看图谱结果
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                name="重生成图谱"
                                                href={buildGraphRegenerateHref(applyFollowUp)}
                                                size="small"
                                            >
                                                重生成图谱
                                            </KuzhambuButton>
                                            <KuzhambuButton
                                                name="重新生成质量报告"
                                                href={buildQualityReportHref(applyFollowUp)}
                                                size="small"
                                            >
                                                重新生成质量报告
                                            </KuzhambuButton>
                                        </KuzhambuSpace>
                                    }
                                    message="精修已应用，图谱与质量报告需要继续联动处理"
                                    showIcon
                                    type="success"
                                />
                            ) : null}
                            <RefinementProgressSummaryPanel summary={progressSummary} />
                            <Row gutter={[16, 16]}>
                                <Col xs={24} lg={16}>
                                    <Card
                                        className="knowledge-refinement-card"
                                        title="实体草稿"
                                        extra={
                                            <a
                                                onClick={() =>
                                                    applyTaskMutation.mutate({
                                                        refinementTaskId: readDetailTaskId(detail),
                                                        appliedBy: 1
                                                    })
                                                }
                                            >
                                                应用任务
                                            </a>
                                        }
                                    >
                                        <RefinementEntityTable
                                            canEdit={canEdit}
                                            entities={detail?.entities || []}
                                            onAdd={() => {
                                                setEditingEntity(null);
                                                setEntityEditorOpen(true);
                                            }}
                                            onAnnotate={(entity) =>
                                                openAnnotation({
                                                    objectType: "ENTITY",
                                                    objectKey: entity.entityKey || "",
                                                    sourceContentType: detail?.sourceContentType,
                                                    sourceContentId: detail?.sourceContentId
                                                })
                                            }
                                            onConfirm={(entity) =>
                                                entityMutation.mutate({
                                                    ...entity,
                                                    confirmationStatus: "MANUAL_CONFIRMED"
                                                })
                                            }
                                            onDelete={setDeletingEntity}
                                            onEdit={(entity) => {
                                                setEditingEntity(entity);
                                                setEntityEditorOpen(true);
                                            }}
                                        />
                                    </Card>
                                </Col>
                                <Col xs={24} lg={8}>
                                    <Card className="knowledge-refinement-card" title="质量汇总">
                                        <dl className="knowledge-refinement-quality-list">
                                            <div>
                                                <dt>实体覆盖率</dt>
                                                <dd>
                                                    {(
                                                        (qualitySummary?.entityCoverageRate || 0) *
                                                        100
                                                    ).toFixed(0)}
                                                    %
                                                </dd>
                                            </div>
                                            <div>
                                                <dt>关系准确率</dt>
                                                <dd>
                                                    {(
                                                        (qualitySummary?.relationAccuracyRate ||
                                                            0) * 100
                                                    ).toFixed(0)}
                                                    %
                                                </dd>
                                            </div>
                                            <div>
                                                <dt>完整度</dt>
                                                <dd>
                                                    {(
                                                        (qualitySummary?.completenessRate || 0) *
                                                        100
                                                    ).toFixed(0)}
                                                    %
                                                </dd>
                                            </div>
                                        </dl>
                                    </Card>
                                </Col>
                            </Row>
                            <Card className="knowledge-refinement-card" title="关系草稿">
                                <RefinementRelationTable
                                    canEdit={canEdit}
                                    onAdd={() => {
                                        setEditingRelation(null);
                                        setRelationEditorOpen(true);
                                    }}
                                    onAnnotate={(relation) =>
                                        openAnnotation({
                                            objectType: "RELATION",
                                            objectKey: relation.relationKey || "",
                                            sourceContentType: detail?.sourceContentType,
                                            sourceContentId: detail?.sourceContentId
                                        })
                                    }
                                    onConfirm={(relation) =>
                                        relationMutation.mutate({
                                            ...relation,
                                            confirmationStatus: "MANUAL_CONFIRMED"
                                        })
                                    }
                                    onDelete={setDeletingRelation}
                                    onEdit={(relation) => {
                                        setEditingRelation(relation);
                                        setRelationEditorOpen(true);
                                    }}
                                    relations={detail?.relations || []}
                                />
                            </Card>
                            <Row gutter={[16, 16]}>
                                <Col xs={24} lg={12}>
                                    <Card
                                        className="knowledge-refinement-card"
                                        title="世系节点草稿"
                                    >
                                        <Table<RefinementLineageNodeRecord>
                                            aria-label="知识图谱精修世系节点表格"
                                            columns={lineageNodeColumns}
                                            dataSource={detail?.lineageNodes || []}
                                            pagination={false}
                                            rowKey={(node) =>
                                                node.draftId ||
                                                node.nodeKey ||
                                                node.name ||
                                                "lineage-node"
                                            }
                                        />
                                    </Card>
                                </Col>
                                <Col xs={24} lg={12}>
                                    <Card
                                        className="knowledge-refinement-card"
                                        title="世系关系草稿"
                                    >
                                        <Table<RefinementLineageRelationRecord>
                                            aria-label="知识图谱精修世系关系表格"
                                            columns={lineageRelationColumns}
                                            dataSource={detail?.lineageRelations || []}
                                            pagination={false}
                                            rowKey={(relation) =>
                                                relation.draftId ||
                                                relation.relationKey ||
                                                relation.relationType ||
                                                "lineage-relation"
                                            }
                                        />
                                    </Card>
                                </Col>
                            </Row>
                            <Card className="knowledge-refinement-card" title="质量标注">
                                <RefinementQualityAnnotationTable
                                    annotations={qualityAnnotations}
                                    loading={qualityAnnotationQuery.isLoading}
                                    onEdit={openAnnotationFromRecord}
                                />
                            </Card>
                        </KuzhambuSpace>
                    ) : (
                        <Card className="knowledge-refinement-card" variant="borderless">
                            <Empty
                                description="请先从上方任务列表打开一条精修任务。"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        </Card>
                    )}
                </section>
            </KuzhambuSpace>

            <RefinementEntityEditor
                entity={editingEntity}
                open={entityEditorOpen}
                saving={entityMutation.isPending}
                onCancel={() => {
                    setEntityEditorOpen(false);
                    setEditingEntity(null);
                }}
                onSubmit={(values) => entityMutation.mutate(values)}
            />
            <RefinementEntityDeleteModal
                deleting={entityDeleteMutation.isPending}
                entity={deletingEntity}
                open={deletingEntity !== null}
                onCancel={() => setDeletingEntity(null)}
                onConfirm={() => {
                    if (deletingEntity) {
                        entityDeleteMutation.mutate(deletingEntity);
                    }
                }}
            />
            <RefinementRelationEditor
                open={relationEditorOpen}
                relation={editingRelation}
                saving={relationMutation.isPending}
                onCancel={() => {
                    setRelationEditorOpen(false);
                    setEditingRelation(null);
                }}
                onSubmit={(values) => relationMutation.mutate(values)}
            />
            <RefinementRelationDeleteModal
                deleting={relationDeleteMutation.isPending}
                open={deletingRelation !== null}
                relation={deletingRelation}
                onCancel={() => setDeletingRelation(null)}
                onConfirm={() => {
                    if (deletingRelation) {
                        relationDeleteMutation.mutate(deletingRelation);
                    }
                }}
            />
            <RefinementQualityAnnotationDrawer
                deleting={annotationDeleteMutation.isPending}
                existingAnnotation={editingAnnotation}
                open={annotationTarget !== null}
                saving={annotationMutation.isPending}
                target={annotationTarget}
                onCancel={() => setAnnotationTarget(null)}
                onDelete={(annotation) => annotationDeleteMutation.mutate(annotation)}
                onSave={(values) => annotationMutation.mutate(values)}
            />
        </KuzhambuPage>
    );
};
