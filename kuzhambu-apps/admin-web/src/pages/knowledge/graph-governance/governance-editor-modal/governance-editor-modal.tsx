import { useMutation, useQuery } from "@tanstack/react-query";
import { App, Form, Input, Spin } from "antd";
import { useEffect } from "react";
import { KuzhambuForm, KuzhambuFormItem, KuzhambuModal, KuzhambuSelect } from "@/components";
import * as service from "../graph-governance-service";
import type {
    GraphGovernanceNodeDetailRecord,
    GraphGovernanceRelationDetailRecord
} from "../graph-governance-types";
import { PublishedNodeRemoteSelect } from "./published-node-remote-select";

const NODE_TYPE_OPTIONS = [
    ["ANIMAL", "动物"],
    ["BUILDING", "建筑"],
    ["CELESTIAL_BODY", "天体"],
    ["CONCEPT", "概念"],
    ["DEITY", "神祇"],
    ["DYNASTY", "朝代"],
    ["EVENT", "事件"],
    ["GROUP", "群体"],
    ["MATERIAL", "材料"],
    ["NATURAL_PHENOMENON", "自然现象"],
    ["OBJECT", "器物"],
    ["OFFICE", "官职"],
    ["ORGANIZATION", "组织"],
    ["PERSON", "人物"],
    ["PLACE", "地点"],
    ["PLANT", "植物"],
    ["RITUAL", "仪式"],
    ["WORK", "著作"]
].map(([value, label]) => ({ label, value }));

const RELATION_TYPE_LABELS: Readonly<Record<string, string>> = {
    ASSOCIATED_WITH: "相关",
    AUTHORED: "撰著",
    DESCRIBES: "记述",
    LOCATED_IN: "位于",
    MENTIONS: "提及",
    PART_OF: "构成/隶属",
    RELATED_TO: "相关",
    USES: "使用/采用"
};

const toRelationLabel = (relationType?: string | null) =>
    (relationType && RELATION_TYPE_LABELS[relationType]) || relationType || "";
const toRelationType = (relationLabel: string) =>
    Object.entries(RELATION_TYPE_LABELS).find(([, label]) => label === relationLabel)?.[0] ??
    relationLabel;

interface GovernanceEditorValues {
    name?: string;
    nodeType?: string;
    reason?: string;
    relationType?: string;
    sourceNodeId?: string;
    targetNodeId?: string;
}

export interface GovernanceEditorTarget {
    id: string;
    type: "NODE" | "EDGE";
}

interface GovernanceEditorModalProps {
    onCancel: () => void;
    onSaved: () => Promise<unknown>;
    target?: GovernanceEditorTarget | null;
}

const readErrorMessage = (error: unknown) =>
    error instanceof Error ? error.message : "保存图谱条目失败";

export const GovernanceEditorModal = ({
    onCancel,
    onSaved,
    target
}: GovernanceEditorModalProps) => {
    const { message } = App.useApp();
    const [form] = Form.useForm<GovernanceEditorValues>();
    const isNode = target?.type === "NODE";
    const detailQuery = useQuery<
        GraphGovernanceNodeDetailRecord | GraphGovernanceRelationDetailRecord
    >({
        enabled: Boolean(target),
        queryFn: () =>
            target?.type === "NODE"
                ? service.getPublishedNode(target.id)
                : service.getPublishedRelation(target?.id ?? ""),
        queryKey: ["knowledge", "graph-governance", "editor", target?.type, target?.id]
    });
    const nodeDetail = isNode
        ? (detailQuery.data as GraphGovernanceNodeDetailRecord | undefined)
        : undefined;
    const edgeDetail = !isNode
        ? (detailQuery.data as GraphGovernanceRelationDetailRecord | undefined)
        : undefined;
    const edgeNodes = [edgeDetail?.sourceNode, edgeDetail?.targetNode].filter(
        (node): node is NonNullable<typeof node> => Boolean(node)
    );

    useEffect(() => {
        if (!target || !detailQuery.data) {
            form.resetFields();
            return;
        }
        if (isNode && nodeDetail) {
            form.setFieldsValue({
                name: nodeDetail.node.name ?? "",
                nodeType: nodeDetail.node.nodeType ?? ""
            });
            return;
        }
        if (edgeDetail) {
            form.setFieldsValue({
                relationType: toRelationLabel(edgeDetail.edge.relationType),
                sourceNodeId: edgeDetail.edge.sourceNodeId ?? "",
                targetNodeId: edgeDetail.edge.targetNodeId ?? ""
            });
        }
    }, [detailQuery.data, edgeDetail, form, isNode, nodeDetail, target]);

    const saveMutation = useMutation({
        mutationFn: async (values: GovernanceEditorValues) => {
            if (isNode && nodeDetail) {
                return service.updatePublishedNode({
                    lockVersion: nodeDetail.node.lockVersion ?? "",
                    node: {
                        id: nodeDetail.node.id,
                        name: values.name ?? "",
                        nodeType: values.nodeType ?? "",
                        properties: {},
                        source: nodeDetail.node.source ?? "MANUAL",
                        status: nodeDetail.node.status ?? "ACTIVE"
                    },
                    properties: nodeDetail.properties.map((property) => ({
                        id: property.id,
                        preferred: property.preferred,
                        propertyName: property.propertyName,
                        value: property.value
                    })),
                    reason: values.reason ?? "编辑发布节点"
                });
            }
            if (edgeDetail) {
                return service.updatePublishedRelation({
                    edge: {
                        id: edgeDetail.edge.id,
                        qualifiers: edgeDetail.edge.qualifiers ?? {},
                        relationType: toRelationType(values.relationType ?? ""),
                        source: edgeDetail.edge.source ?? "MANUAL",
                        sourceNodeId: values.sourceNodeId ?? "",
                        status: edgeDetail.edge.status ?? "ACTIVE",
                        targetNodeId: values.targetNodeId ?? ""
                    },
                    lockVersion: edgeDetail.edge.lockVersion ?? "",
                    properties: edgeDetail.properties.map((property) => ({
                        id: property.id,
                        preferred: property.preferred,
                        propertyName: property.propertyName,
                        value: property.value
                    })),
                    reason: values.reason ?? "编辑发布关系"
                });
            }
            throw new Error("图谱条目尚未加载完成。");
        },
        onError: (error) => message.error(readErrorMessage(error)),
        onSuccess: async () => {
            message.success("发布图谱已更新");
            await onSaved();
            onCancel();
        }
    });

    return (
        <KuzhambuModal
            confirmLoading={saveMutation.isPending}
            forceRender
            open={Boolean(target)}
            testId="knowledge-graph-governance-editor-modal"
            title={isNode ? "编辑节点" : "编辑关系"}
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            {detailQuery.isLoading ? <Spin /> : null}
            <KuzhambuForm form={form} onFinish={(values) => saveMutation.mutate(values)}>
                {isNode ? (
                    <>
                        <KuzhambuFormItem
                            label="节点名称"
                            name="name"
                            rules={[{ required: true, message: "请输入节点名称" }]}
                        >
                            <Input />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="节点类型"
                            name="nodeType"
                            rules={[{ required: true, message: "请选择节点类型" }]}
                        >
                            <KuzhambuSelect options={NODE_TYPE_OPTIONS} showSearch />
                        </KuzhambuFormItem>
                    </>
                ) : (
                    <>
                        <KuzhambuFormItem
                            label="S"
                            name="sourceNodeId"
                            rules={[{ required: true, message: "请选择 S" }]}
                        >
                            <PublishedNodeRemoteSelect
                                ariaLabel="搜索 S 节点"
                                initialNodes={edgeNodes}
                            />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="P"
                            name="relationType"
                            rules={[{ required: true, message: "请输入关系类型" }]}
                        >
                            <Input />
                        </KuzhambuFormItem>
                        <KuzhambuFormItem
                            label="O"
                            name="targetNodeId"
                            rules={[{ required: true, message: "请选择 O" }]}
                        >
                            <PublishedNodeRemoteSelect
                                ariaLabel="搜索 O 节点"
                                initialNodes={edgeNodes}
                            />
                        </KuzhambuFormItem>
                    </>
                )}
                <KuzhambuFormItem
                    label="变更原因"
                    name="reason"
                    rules={[{ required: true, message: "请填写变更原因" }]}
                >
                    <Input.TextArea maxLength={1024} rows={3} />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuModal>
    );
};
