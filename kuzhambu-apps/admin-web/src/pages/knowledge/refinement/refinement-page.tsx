import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, App, Card, Col, Empty, Row, Space, Typography } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE } from "@/types/page";
import { RefinementEntityDeleteModal } from "./components/refinement-entity-delete-modal";
import { RefinementEntityEditor } from "./components/refinement-entity-editor";
import { RefinementEntityTable } from "./components/refinement-entity-table";
import { RefinementFilterForm } from "./components/refinement-filter-form";
import { RefinementProgressSummaryPanel } from "./components/refinement-progress-summary";
import { RefinementRelationDeleteModal } from "./components/refinement-relation-delete-modal";
import { RefinementRelationEditor } from "./components/refinement-relation-editor";
import { RefinementRelationTable } from "./components/refinement-relation-table";
import { RefinementWorkbenchTable } from "./components/refinement-workbench-table";
import * as service from "./refinement-service";
import type {
    RefinementDetailRecord,
    RefinementEntityRecord,
    RefinementRelationRecord,
    RefinementTaskPageQuery
} from "./refinement-types";
import "./refinement-page.css";

const { Text, Title } = Typography;

const readDetailTaskId = (detail: RefinementDetailRecord | null) => detail?.refinementTaskId ?? 0;

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

    const refreshDetail = async (refinementTaskId: number) => {
        const nextDetail = await service.getTaskDetail({ refinementTaskId });
        setDetail(nextDetail);
        await Promise.all([
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "quality-summary", refinementTaskId]
            }),
            queryClient.invalidateQueries({
                queryKey: ["knowledge", "refinement", "tasks"]
            })
        ]);
        return nextDetail;
    };

    const openTaskMutation = useMutation({
        mutationFn: service.getTaskDraft,
        onSuccess: async (nextDetail) => {
            setDetail(nextDetail);
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
        onSuccess: async (nextDetail) => {
            setDetail(nextDetail);
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

    const progressSummary = detail?.progressSummary ?? null;
    const qualitySummary = qualitySummaryQuery.data;
    const taskItems = taskPageQuery.data?.records || [];
    const detailReady = detail !== null;
    const detailEyebrow = useMemo(() => {
        if (!detail) {
            return "先从左侧任务列表打开一条精修任务";
        }
        return `${detail.taskType || "GRAPH"} / ${detail.sourceCategoryName || detail.sourceCategoryCode || "-"}`;
    }, [detail]);

    return (
        <KuzhambuPage
            className="knowledge-refinement-page refinement-page"
            description="围绕待精修任务完成实体、关系确认和应用回正式事实。"
            eyebrow="Knowledge / Refinement"
            title="知识图谱精修工作台"
        >
            <Space className="knowledge-refinement-layout" direction="vertical" size={16}>
                <Alert
                    banner
                    message="本页支持打开精修任务、修订实体关系草稿并应用回正式事实。世系结构已打通后端契约，但当前页面暂不开放交互。"
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
                        <Space direction="vertical" size={16} style={{ width: "100%" }}>
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
                        </Space>
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
                        <Space direction="vertical" size={16} style={{ width: "100%" }}>
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
                        </Space>
                    ) : (
                        <Card className="knowledge-refinement-card" variant="borderless">
                            <Empty
                                description="请先从上方任务列表打开一条精修任务。"
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                            />
                        </Card>
                    )}
                </section>
            </Space>

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
        </KuzhambuPage>
    );
};
