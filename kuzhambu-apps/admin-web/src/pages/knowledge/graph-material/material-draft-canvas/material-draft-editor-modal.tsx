import { Form, Input } from "antd";
import { useEffect } from "react";
import { KuzhambuForm, KuzhambuFormItem, KuzhambuModal, KuzhambuSelect } from "@/components";
import type {
    GraphMaterialEdgeRecord,
    GraphMaterialNodeRecord
} from "@/pages/knowledge/graph-material/graph-material-types";

const NODE_TYPE_LABELS: Readonly<Record<string, string>> = {
    ANIMAL: "动物",
    BUILDING: "建筑",
    CELESTIAL_BODY: "天体",
    CONCEPT: "概念",
    DEITY: "神祇",
    DYNASTY: "朝代",
    EVENT: "事件",
    GROUP: "群体",
    MATERIAL: "材料",
    NATURAL_PHENOMENON: "自然现象",
    OBJECT: "器物",
    OFFICE: "官职",
    ORGANIZATION: "组织",
    PERSON: "人物",
    PLACE: "地点",
    PLANT: "植物",
    RITUAL: "仪式",
    WORK: "著作"
};

const NODE_TYPE_OPTIONS = Object.entries(NODE_TYPE_LABELS).map(([value, label]) => ({
    label,
    value
}));

export interface GraphDraftEditorValues {
    objectName?: string;
    objectNodeId?: string;
    objectNodeType?: string;
    predicate?: string;
    subjectName?: string;
    subjectNodeId?: string;
    subjectNodeType?: string;
}

export interface GraphDraftEditorItem {
    edge?: GraphMaterialEdgeRecord;
    node?: GraphMaterialNodeRecord;
}

export type GraphDraftEditorMode = "EDGE" | "NODE";

interface MaterialDraftEditorModalProps {
    initialValues?: Partial<GraphDraftEditorValues>;
    item?: GraphDraftEditorItem | null;
    mode: GraphDraftEditorMode;
    nodes: GraphMaterialNodeRecord[];
    open: boolean;
    relationTypes: string[];
    saving?: boolean;
    onCancel: () => void;
    onSubmit: (values: GraphDraftEditorValues) => void;
}

const toNodeOptions = (nodes: GraphMaterialNodeRecord[]) =>
    nodes.map((node) => ({
        label: `${node.name}（${NODE_TYPE_LABELS[node.nodeType] ?? node.nodeType}）`,
        value: node.id
    }));

export const MaterialDraftEditorModal = ({
    initialValues,
    item,
    mode,
    nodes,
    open,
    relationTypes,
    saving = false,
    onCancel,
    onSubmit
}: MaterialDraftEditorModalProps) => {
    const [form] = Form.useForm<GraphDraftEditorValues>();
    const isNodeEditor = mode === "NODE";
    const nodeOptions = toNodeOptions(nodes);

    useEffect(() => {
        if (!open) {
            form.resetFields();
            return;
        }
        if (mode === "NODE" && item?.node) {
            form.setFieldsValue({
                subjectName: item.node.name,
                subjectNodeType: item.node.nodeType
            });
            return;
        }
        if (mode === "EDGE" && item?.edge) {
            form.setFieldsValue({
                objectNodeId: item.edge.targetNodeId,
                predicate: item.edge.relationType,
                subjectNodeId: item.edge.sourceNodeId
            });
            return;
        }
        form.setFieldsValue(
            mode === "NODE" ? { subjectNodeType: "CONCEPT" } : (initialValues ?? {})
        );
    }, [form, initialValues, item, mode, open]);

    return (
        <KuzhambuModal
            confirmLoading={saving}
            forceRender
            open={open}
            testId="knowledge-graph-material-draft-editor-modal"
            title={
                isNodeEditor
                    ? item?.node
                        ? "编辑节点"
                        : "新建节点"
                    : item?.edge
                      ? "编辑边"
                      : "新建边"
            }
            onCancel={onCancel}
            onOk={() => form.submit()}
        >
            <KuzhambuForm form={form} onFinish={onSubmit}>
                <KuzhambuFormItem
                    label={isNodeEditor ? "节点名称" : "S"}
                    name={isNodeEditor ? "subjectName" : "subjectNodeId"}
                    rules={[
                        { required: true, message: isNodeEditor ? "请输入节点名称" : "请选择 S" }
                    ]}
                >
                    {isNodeEditor ? (
                        <Input />
                    ) : (
                        <KuzhambuSelect
                            allowClear
                            options={nodeOptions}
                            placeholder="选择已有主体"
                            showSearch
                        />
                    )}
                </KuzhambuFormItem>
                {isNodeEditor ? (
                    <KuzhambuFormItem label="节点类型" name="subjectNodeType">
                        <KuzhambuSelect options={NODE_TYPE_OPTIONS} showSearch />
                    </KuzhambuFormItem>
                ) : null}
                {!isNodeEditor ? (
                    <>
                        <KuzhambuFormItem
                            label="P（选择或输入）"
                            name="predicate"
                            rules={[{ required: true, message: "请选择或输入关系" }]}
                        >
                            <Input
                                list="knowledge-graph-material-relation-types"
                                placeholder="例如：作者、位于、属于"
                            />
                        </KuzhambuFormItem>
                        <datalist id="knowledge-graph-material-relation-types">
                            {relationTypes.map((relationType) => (
                                <option key={relationType} value={relationType} />
                            ))}
                        </datalist>
                        <KuzhambuFormItem
                            label="O"
                            name="objectNodeId"
                            rules={[{ required: true, message: "请选择 O" }]}
                        >
                            <KuzhambuSelect
                                allowClear
                                options={nodeOptions}
                                placeholder="选择已有客体"
                                showSearch
                            />
                        </KuzhambuFormItem>
                    </>
                ) : null}
            </KuzhambuForm>
        </KuzhambuModal>
    );
};
