import { useMutation } from "@tanstack/react-query";
import type { Key, ReactNode } from "react";
import { useMemo, useState } from "react";
import { App, Checkbox, Empty, Typography } from "antd";
import {
    BankOutlined,
    BookOutlined,
    BugOutlined,
    CloudOutlined,
    EnvironmentOutlined,
    GlobalOutlined,
    HomeOutlined,
    MedicineBoxOutlined,
    TeamOutlined,
    ToolOutlined,
    UserOutlined
} from "@ant-design/icons";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import {
    KuzhambuCard,
    KuzhambuGraph,
    KuzhambuModal,
    KuzhambuSelect,
    KuzhambuSpace,
    KuzhambuTable,
    KuzhambuTag,
    type KuzhambuTableProps
} from "@/components";
import * as service from "@/pages/knowledge/graph-material/graph-material-service";
import type {
    GraphMaterialDetailRecord,
    GraphMaterialEdgeRecord,
    GraphMaterialNodeRecord,
    GraphMaterialRecord
} from "@/pages/knowledge/graph-material/graph-material-types";
import {
    MaterialDraftEditorModal,
    type GraphDraftEditorItem,
    type GraphDraftEditorMode,
    type GraphDraftEditorValues
} from "./material-draft-editor-modal";

const { Text } = Typography;

const RELATION_TYPE_LABELS: Readonly<Record<string, string>> = {
    ANCESTOR_OF: "祖先/后裔",
    ASSOCIATED_WITH: "相关",
    AUTHORED: "撰著",
    CAUSES: "导致/引起",
    COMPILED: "编纂",
    DEPICTS: "描绘",
    DESCRIBES: "记述",
    HOLDS_OFFICE: "任职",
    LOCATED_IN: "位于",
    MADE_OF: "制成材料",
    MEMBER_OF: "隶属/成员",
    MENTIONS: "提及",
    OCCURS_AT: "发生于",
    PARENT_OF: "父母/子女",
    PARTICIPATED_IN: "参与",
    PART_OF: "构成/隶属",
    PRACTICES: "实行/奉行",
    RELATED_TO: "相关",
    RULES: "统治/管辖",
    SPOUSE_OF: "配偶",
    SUCCEEDS: "继承/取代",
    USES: "使用/采用",
    WORSHIPS: "崇祀"
};

interface GraphDraftEdgeTableItem {
    edge: GraphMaterialEdgeRecord;
    id: string;
    kind: "EDGE";
}

interface GraphDraftNodeTableItem {
    children: GraphDraftEdgeTableItem[];
    id: string;
    kind: "NODE";
    node: GraphMaterialNodeRecord;
}

type GraphDraftTableItem = GraphDraftEdgeTableItem | GraphDraftNodeTableItem;

interface GraphDraftEditorState {
    initialValues?: Partial<GraphDraftEditorValues>;
    item?: GraphDraftEditorItem;
    mode: GraphDraftEditorMode;
}

interface MaterialDraftCanvasProps {
    canEditGraph: boolean;
    detail: GraphMaterialDetailRecord | null;
    material: GraphMaterialRecord;
    onRefresh: () => Promise<unknown>;
}

const readErrorMessage = (error: unknown, fallback: string) =>
    error instanceof Error ? error.message : fallback;

const toTableItems = (detail: GraphMaterialDetailRecord | null): GraphDraftNodeTableItem[] =>
    (detail?.nodes ?? []).map((node) => ({
        children: (detail?.edges ?? [])
            .filter((edge) => edge.sourceNodeId === node.id || edge.targetNodeId === node.id)
            .map((edge) => ({
                edge,
                id: `node:${node.id}:edge:${edge.id}`,
                kind: "EDGE" as const
            })),
        id: `node:${node.id}`,
        kind: "NODE" as const,
        node
    }));

const readNode = (nodes: GraphMaterialNodeRecord[], nodeId: string) =>
    nodes.find((node) => node.id === nodeId);

const readNodeTypeIcon = (nodeType: string) => {
    const iconByType: Record<string, ReactNode> = {
        ANIMAL: <BugOutlined />,
        BUILDING: <HomeOutlined />,
        CELESTIAL_BODY: <GlobalOutlined />,
        CONCEPT: <ToolOutlined />,
        DEITY: <CloudOutlined />,
        DYNASTY: <BankOutlined />,
        EVENT: <GlobalOutlined />,
        GROUP: <TeamOutlined />,
        MATERIAL: <MedicineBoxOutlined />,
        NATURAL_PHENOMENON: <CloudOutlined />,
        OBJECT: <ToolOutlined />,
        OFFICE: <BankOutlined />,
        ORGANIZATION: <BankOutlined />,
        PERSON: <UserOutlined />,
        PLACE: <EnvironmentOutlined />,
        PLANT: <BugOutlined />,
        RITUAL: <BookOutlined />,
        WORK: <BookOutlined />
    };
    return <span title={`节点类型：${nodeType}`}>{iconByType[nodeType] ?? <ToolOutlined />}</span>;
};

const readRelationTypeLabel = (relationType: string) =>
    RELATION_TYPE_LABELS[relationType] ?? relationType;

const toRelationType = (label: string) =>
    Object.entries(RELATION_TYPE_LABELS).find(
        ([, relationLabel]) => relationLabel === label
    )?.[0] ?? label;

export const MaterialDraftCanvas = ({
    canEditGraph,
    detail,
    material,
    onRefresh
}: MaterialDraftCanvasProps) => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editorState, setEditorState] = useState<GraphDraftEditorState | undefined>();
    const [mergeDialogOpen, setMergeDialogOpen] = useState(false);
    const [mergeNodeIds, setMergeNodeIds] = useState<string[]>([]);
    const [retainedNodeId, setRetainedNodeId] = useState<string | undefined>();
    const isMaterialMutationLocked =
        material.status === "PUBLISHED" ||
        material.status === "PUBLISHING" ||
        material.status === "WITHDRAWING";
    const canMutateDraft = canEditGraph && !isMaterialMutationLocked;
    const tableItems = useMemo(() => toTableItems(detail), [detail]);
    const selectedNodes = tableItems
        .filter((item) => selectedRowKeys.includes(item.id))
        .map((item) => item.node);
    const graphNodeList = useMemo(
        () =>
            (detail?.nodes ?? []).map((node) => ({
                group: node.nodeType,
                id: node.id,
                label: node.name
            })),
        [detail]
    );
    const spoList = useMemo(
        () =>
            (detail?.edges ?? []).map((edge) => {
                const sourceNode = readNode(detail?.nodes ?? [], edge.sourceNodeId);
                const targetNode = readNode(detail?.nodes ?? [], edge.targetNodeId);
                return {
                    object: targetNode?.name ?? edge.targetNodeId,
                    objectId: edge.targetNodeId,
                    predicate: edge.relationType,
                    subject: sourceNode?.name ?? edge.sourceNodeId,
                    subjectId: edge.sourceNodeId
                };
            }),
        [detail]
    );
    const editMutation = useMutation({
        mutationFn: async (values: GraphDraftEditorValues) => {
            const editingItem = editorState?.item;
            if (editorState?.mode === "NODE") {
                if (!editingItem?.node) {
                    return service.createMaterialNode({
                        contentRef: material.contentRef,
                        materialLockVersion: material.lockVersion ?? "",
                        node: {
                            name: values.subjectName ?? "",
                            nodeType: values.subjectNodeType ?? "CONCEPT",
                            properties: {},
                            source: "MANUAL"
                        }
                    });
                }
                return service.updateMaterialNode({
                    contentRef: material.contentRef,
                    materialLockVersion: material.lockVersion ?? "",
                    node: {
                        id: editingItem.node.id,
                        name: values.subjectName ?? "",
                        nodeType: values.subjectNodeType ?? editingItem.node.nodeType,
                        properties: editingItem.node.properties,
                        source: "MANUAL"
                    }
                });
            }
            if (!values.subjectNodeId || !values.objectNodeId) {
                throw new Error("请选择已有的 S 和 O 节点。");
            }
            const edge = editingItem?.edge;
            const command = {
                contentRef: material.contentRef,
                edge: {
                    ...(edge ? { id: edge.id } : {}),
                    qualifiers: edge?.qualifiers ?? {},
                    relationType: toRelationType(values.predicate ?? ""),
                    source: "MANUAL" as const,
                    sourceNodeId: values.subjectNodeId,
                    targetNodeId: values.objectNodeId
                },
                materialLockVersion: material.lockVersion ?? ""
            };
            return edge ? service.updateMaterialEdge(command) : service.createMaterialEdge(command);
        },
        onSuccess: async () => {
            setEditorState(undefined);
            await onRefresh();
            messageApi.success("草稿图谱已更新");
        },
        onError: (error) => messageApi.error(readErrorMessage(error, "保存图谱条目失败"))
    });
    const deleteMutation = useMutation({
        mutationFn: (item: GraphDraftTableItem) => {
            const command = {
                contentRef: material.contentRef,
                materialLockVersion: material.lockVersion ?? "",
                objectId: item.kind === "NODE" ? item.node.id : item.edge.id
            };
            return item.kind === "NODE"
                ? service.deleteMaterialNode(command)
                : service.deleteMaterialEdge(command);
        },
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await onRefresh();
            messageApi.success("图谱条目已删除");
        },
        onError: (error) => messageApi.error(readErrorMessage(error, "删除图谱条目失败"))
    });
    const batchDeleteMutation = useMutation({
        mutationFn: async () => {
            let currentMaterial = material;
            for (const node of selectedNodes) {
                const command = {
                    contentRef: currentMaterial.contentRef,
                    materialLockVersion: currentMaterial.lockVersion ?? "",
                    objectId: node.id
                };
                const result = await service.deleteMaterialNode(command);
                currentMaterial = result.material ?? currentMaterial;
            }
        },
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await onRefresh();
            messageApi.success("已批量删除节点");
        },
        onError: (error) => messageApi.error(readErrorMessage(error, "批量删除节点失败"))
    });
    const mergeMutation = useMutation({
        mutationFn: () => {
            if (!retainedNodeId || mergeNodeIds.length < 2) {
                throw new Error("请选择至少两个节点，并指定保留节点。");
            }
            return service.mergeMaterialNodes({
                contentRef: material.contentRef,
                materialLockVersion: material.lockVersion ?? "",
                mergedNodeIds: mergeNodeIds.filter((nodeId) => nodeId !== retainedNodeId),
                retainedNodeId
            });
        },
        onSuccess: async () => {
            setSelectedRowKeys([]);
            setMergeDialogOpen(false);
            setMergeNodeIds([]);
            setRetainedNodeId(undefined);
            await onRefresh();
            messageApi.success("节点已合并");
        },
        onError: (error) => messageApi.error(readErrorMessage(error, "合并节点失败"))
    });
    const confirmDelete = (item: GraphDraftTableItem) => {
        const label =
            item.kind === "NODE"
                ? item.node.name
                : `${readNode(detail?.nodes ?? [], item.edge.sourceNodeId)?.name ?? "S"} ${item.edge.relationType} ${readNode(detail?.nodes ?? [], item.edge.targetNodeId)?.name ?? "O"}`;
        confirm.danger({
            title: "删除图谱条目",
            message: `确认删除“${label}”？`,
            description: "删除节点可能同时影响其关联关系，后端会按图谱约束校验。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(item)
        });
    };
    const openMergeDialog = () => {
        const nodeIds = selectedNodes.map((node) => node.id);
        setMergeNodeIds(nodeIds);
        setRetainedNodeId(nodeIds[0]);
        setMergeDialogOpen(true);
    };
    const confirmBatchDelete = () => {
        if (!selectedNodes.length) {
            return;
        }
        confirm.danger({
            title: "批量删除节点",
            message: `确认删除选中的 ${selectedNodes.length} 个节点？`,
            description: "删除节点可能同时影响其关联关系，后端会按图谱约束校验。",
            okText: "删除",
            onConfirm: () => batchDeleteMutation.mutateAsync()
        });
    };
    const columns: KuzhambuTableProps<GraphDraftTableItem>["columns"] = [
        {
            title: "图谱信息",
            key: "information",
            render: (_, item) => {
                if (item.kind === "NODE") {
                    return (
                        <KuzhambuSpace size={8}>
                            {readNodeTypeIcon(item.node.nodeType)}
                            <Text>{item.node.name}</Text>
                            <KuzhambuTag type={item.children.length === 0 ? "danger" : "neutral"}>
                                {item.children.length === 0
                                    ? "孤立节点"
                                    : `${item.children.length} 条关联边`}
                            </KuzhambuTag>
                        </KuzhambuSpace>
                    );
                }
                const subject = readNode(detail?.nodes ?? [], item.edge.sourceNodeId);
                const object = readNode(detail?.nodes ?? [], item.edge.targetNodeId);
                return (
                    <KuzhambuSpace size={8}>
                        {readNodeTypeIcon(subject?.nodeType ?? "UNKNOWN")}
                        <Text>{subject?.name ?? item.edge.sourceNodeId}</Text>
                        <KuzhambuTag type="neutral">
                            {readRelationTypeLabel(item.edge.relationType)}
                        </KuzhambuTag>
                        {readNodeTypeIcon(object?.nodeType ?? "UNKNOWN")}
                        <Text>{object?.name ?? item.edge.targetNodeId}</Text>
                    </KuzhambuSpace>
                );
            }
        },
        {
            key: "actions",
            options: (item) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: "编辑图谱条目",
                    disabled: !canMutateDraft,
                    onClick: () =>
                        setEditorState({
                            item: item.kind === "NODE" ? { node: item.node } : { edge: item.edge },
                            mode: item.kind === "NODE" ? "NODE" : "EDGE"
                        }),
                    testId: `knowledge-graph-material-draft-edit-${item.id}-button`
                },
                ...(item.kind === "NODE"
                    ? [
                          {
                              key: "add-edge",
                              text: "加边",
                              ariaLabel: "从当前节点加边",
                              disabled: !canMutateDraft,
                              onClick: () =>
                                  setEditorState({
                                      initialValues: { subjectNodeId: item.node.id },
                                      mode: "EDGE"
                                  }),
                              testId: `knowledge-graph-material-draft-add-edge-${item.id}-button`
                          }
                      ]
                    : []),
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: "删除图谱条目",
                    disabled: !canMutateDraft,
                    onClick: () => confirmDelete(item),
                    testId: `knowledge-graph-material-draft-delete-${item.id}-button`
                }
            ]
        }
    ];

    return (
        <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
            <KuzhambuCard>
                <KuzhambuTable<GraphDraftTableItem>
                    ariaLabel="素材草稿图谱条目"
                    columns={columns}
                    dataSource={tableItems}
                    expandable={{
                        rowExpandable: (item) => item.kind === "NODE" && item.children.length > 0
                    }}
                    locale={{ emptyText: "暂无草稿图谱条目" }}
                    pagination={false}
                    rowKey="id"
                    rowSelection={{
                        selectedRowKeys,
                        getCheckboxProps: (item) => ({ disabled: item.kind === "EDGE" }),
                        onChange: setSelectedRowKeys
                    }}
                    toolbar={{
                        actions: [
                            {
                                action: () => setEditorState({ mode: "NODE" }),
                                disabled: !canMutateDraft,
                                testId: "knowledge-graph-material-draft-create-button",
                                title: "新建节点",
                                type: "primary"
                            },
                            {
                                action: openMergeDialog,
                                disabled: !canMutateDraft,
                                testId: "knowledge-graph-material-draft-merge-button",
                                title: "合并节点"
                            },
                            {
                                action: confirmBatchDelete,
                                danger: true,
                                disabled: !canMutateDraft || selectedNodes.length === 0,
                                loading: batchDeleteMutation.isPending,
                                testId: "knowledge-graph-material-draft-batch-delete-button",
                                title: "批量删除"
                            }
                        ]
                    }}
                />
            </KuzhambuCard>
            <KuzhambuCard>
                {graphNodeList.length > 0 ? (
                    <KuzhambuGraph height={300} nodeList={graphNodeList} spoList={spoList} />
                ) : (
                    <Empty description="暂无可视化节点" />
                )}
            </KuzhambuCard>
            <MaterialDraftEditorModal
                initialValues={editorState?.initialValues}
                item={editorState?.item}
                mode={editorState?.mode ?? "NODE"}
                nodes={detail?.nodes ?? []}
                open={editorState !== undefined}
                relationTypes={Array.from(
                    new Set(
                        (detail?.edges ?? []).map((edge) =>
                            readRelationTypeLabel(edge.relationType)
                        )
                    )
                )}
                saving={editMutation.isPending}
                onCancel={() => setEditorState(undefined)}
                onSubmit={(values) => editMutation.mutate(values)}
            />
            <KuzhambuModal
                cancelText="取消"
                confirmLoading={mergeMutation.isPending}
                okButtonProps={{
                    danger: true,
                    disabled: !retainedNodeId || mergeNodeIds.length < 2
                }}
                okText="合并节点"
                open={mergeDialogOpen}
                testId="knowledge-graph-material-draft-merge-modal"
                title="合并节点"
                onCancel={() => setMergeDialogOpen(false)}
                onOk={() => mergeMutation.mutate()}
            >
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    <Text type="secondary">
                        选择同一实体的两个或更多节点。关联边会迁移到保留节点，其他节点将被删除。
                    </Text>
                    <Checkbox.Group
                        aria-label="选择要合并的节点"
                        options={(detail?.nodes ?? []).map((node) => ({
                            label: node.name,
                            value: node.id
                        }))}
                        value={mergeNodeIds}
                        onChange={(nodeIds) => {
                            const nextNodeIds = nodeIds.map(String);
                            setMergeNodeIds(nextNodeIds);
                            if (!retainedNodeId || !nextNodeIds.includes(retainedNodeId)) {
                                setRetainedNodeId(nextNodeIds[0]);
                            }
                        }}
                    />
                    <KuzhambuSelect
                        aria-label="保留节点"
                        disabled={mergeNodeIds.length < 2}
                        options={(detail?.nodes ?? [])
                            .filter((node) => mergeNodeIds.includes(node.id))
                            .map((node) => ({ label: node.name, value: node.id }))}
                        placeholder="选择保留节点"
                        value={retainedNodeId}
                        onChange={setRetainedNodeId}
                    />
                </KuzhambuSpace>
            </KuzhambuModal>
        </KuzhambuSpace>
    );
};
