import { Card, Checkbox, Form, Input, InputNumber } from "antd";
import { useEffect } from "react";
import { KuzhambuForm, KuzhambuFormItem } from "@/components/kuzhambu-form";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type {
    GraphExtractionCreateCommand,
    GraphExtractionRegenerateCommand,
    GraphExtractionTaskRecord,
    GraphExtractionTaskType
} from "../graph-extraction-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";

const { TextArea } = Input;

interface GraphExtractionCreateProps {
    canEdit?: boolean;
    creatingTaskType?: GraphExtractionTaskType | null;
    latestCreatedTask?: GraphExtractionTaskRecord | null;
    onCreate: (request: GraphExtractionCreateCommand) => void;
    onRegenerate?: (request: GraphExtractionRegenerateCommand) => void;
    regenerateCommand?: GraphExtractionRegenerateCommand | null;
    regenerating?: boolean;
}

interface GraphExtractionCreateFormValues {
    inputPayloadJson: string;
    locale?: string;
    modelId: number;
    modelName: string;
    promptMessagesJson: string;
    replaceUnconfirmedOnly?: boolean;
    selectionScopeJson?: string;
    scopeJson?: string;
    scopeType?: string;
    sourceContentId: number;
    sourceContentType: string;
}

const normalizeValue = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const createEventId = (prefix: string) => {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
        return `${prefix}-${crypto.randomUUID()}`;
    }
    return `${prefix}-${Date.now()}`;
};

const CREATE_ACTIONS: { key: GraphExtractionTaskType; label: string }[] = [
    { key: "RELATION", label: "创建关系抽取任务" },
    { key: "GRAPH", label: "创建图谱抽取任务" },
    { key: "LINEAGE", label: "创建世系抽取任务" }
];

const toCreateRequest = (
    taskType: GraphExtractionTaskType,
    values: GraphExtractionCreateFormValues
): GraphExtractionCreateCommand => ({
    taskType,
    selectionScopeJson: normalizeValue(values.selectionScopeJson),
    replaceUnconfirmedOnly: values.replaceUnconfirmedOnly ?? undefined,
    scopeType: normalizeValue(values.scopeType),
    scopeJson: normalizeValue(values.scopeJson),
    sourceContentType: values.sourceContentType.trim(),
    sourceContentId: values.sourceContentId,
    modelId: values.modelId,
    modelName: values.modelName.trim(),
    requestId: createEventId("graph-task"),
    traceId: createEventId("graph-trace"),
    promptMessagesJson: values.promptMessagesJson.trim(),
    inputPayloadJson: values.inputPayloadJson.trim(),
    locale: normalizeValue(values.locale) || "zh-CN"
});

export const GraphExtractionCreate = ({
    canEdit = false,
    creatingTaskType,
    latestCreatedTask,
    onCreate,
    onRegenerate,
    regenerateCommand = null,
    regenerating = false
}: GraphExtractionCreateProps) => {
    const [form] = Form.useForm<GraphExtractionCreateFormValues>();
    const regenerateReady = Boolean(regenerateCommand?.sourceTaskId && onRegenerate);

    useEffect(() => {
        if (!regenerateCommand) {
            return;
        }
        form.setFieldsValue({
            selectionScopeJson: regenerateCommand.selectionScopeJson || undefined,
            replaceUnconfirmedOnly: regenerateCommand.replaceUnconfirmedOnly ?? true
        });
    }, [form, regenerateCommand]);

    const submitTask = async (taskType: GraphExtractionTaskType) => {
        const values = await form.validateFields();
        onCreate(toCreateRequest(taskType, values));
    };
    const submitRegenerate = () => {
        if (!regenerateCommand || !onRegenerate) {
            return;
        }
        const values = form.getFieldsValue(["selectionScopeJson", "replaceUnconfirmedOnly"]);
        onRegenerate({
            ...regenerateCommand,
            selectionScopeJson:
                normalizeValue(values.selectionScopeJson) || regenerateCommand.selectionScopeJson,
            replaceUnconfirmedOnly: values.replaceUnconfirmedOnly ?? true
        });
    };

    return (
        <KuzhambuSpace orientation="vertical" size={16} className="graph-extraction-create">
            {regenerateCommand ? (
                <Card className="graph-extraction-create-card" variant="borderless">
                    <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                        <KuzhambuAlert
                            showIcon
                            type="warning"
                            title="精修应用后的图谱重生成参数已载入"
                        />
                        <KuzhambuForm component="div">
                            <KuzhambuFormItem label="源任务 ID">
                                <Input value={regenerateCommand.sourceTaskId || ""} disabled />
                            </KuzhambuFormItem>
                            <KuzhambuFormItem label="任务类型">
                                <Input value={regenerateCommand.taskType || ""} disabled />
                            </KuzhambuFormItem>
                            <KuzhambuFormItem label="触发来源">
                                <Input value={regenerateCommand.triggerSource || ""} disabled />
                            </KuzhambuFormItem>
                        </KuzhambuForm>
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-graph-extraction-create-action-button"
                            type="primary"
                            disabled={!canEdit || !regenerateReady}
                            loading={regenerating}
                            onClick={submitRegenerate}
                        >
                            提交精修重生成
                        </KuzhambuButton>
                    </KuzhambuSpace>
                </Card>
            ) : null}
            <Card className="graph-extraction-create-card" variant="borderless">
                <KuzhambuForm<GraphExtractionCreateFormValues>
                    form={form}
                    initialValues={{
                        locale: "zh-CN",
                        modelId: 1,
                        replaceUnconfirmedOnly: true
                    }}
                >
                    <KuzhambuFormItem
                        name="sourceContentType"
                        label="来源内容类型"
                        rules={[{ required: true, message: "请输入来源内容类型" }]}
                    >
                        <Input placeholder="例如：SANCAI_ENTRY" />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="sourceContentId"
                        label="来源内容 ID"
                        rules={[{ required: true, message: "请输入来源内容 ID" }]}
                    >
                        <InputNumber min={1} precision={0} style={{ width: "100%" }} />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem name="scopeType" label="作用域类型">
                        <Input placeholder="例如：CLASSICS_ENTRY" />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem name="locale" label="语言">
                        <Input placeholder="例如：zh-CN" />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="modelId"
                        label="模型 ID"
                        rules={[{ required: true, message: "请输入模型 ID" }]}
                    >
                        <InputNumber min={1} precision={0} style={{ width: "100%" }} />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="modelName"
                        label="模型名"
                        rules={[{ required: true, message: "请输入模型名" }]}
                    >
                        <Input placeholder="例如：gpt-5.5" />
                    </KuzhambuFormItem>

                    <KuzhambuFormItem name="scopeJson" label="作用域 JSON" layoutSize="large">
                        <TextArea rows={3} placeholder='例如：{"entryId":1001}' />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="selectionScopeJson"
                        label="批量范围 JSON"
                        layoutSize="large"
                    >
                        <TextArea rows={3} placeholder='例如：{"sourceContentIds":[1001,1002]}' />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="replaceUnconfirmedOnly"
                        valuePropName="checked"
                        label="重生成策略"
                    >
                        <Checkbox>仅替换未人工确认结果</Checkbox>
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="promptMessagesJson"
                        label="Prompt Messages JSON"
                        layoutSize="large"
                        rules={[{ required: true, message: "请输入 Prompt Messages JSON" }]}
                    >
                        <TextArea
                            rows={4}
                            placeholder='例如：[{"role":"system","content":"extract"}]'
                        />
                    </KuzhambuFormItem>
                    <KuzhambuFormItem
                        name="inputPayloadJson"
                        label="输入 Payload JSON"
                        layoutSize="large"
                        rules={[{ required: true, message: "请输入输入 Payload JSON" }]}
                    >
                        <TextArea rows={5} placeholder='例如：{"content":"待抽取正文"}' />
                    </KuzhambuFormItem>
                </KuzhambuForm>
                <KuzhambuSpace wrap>
                    {CREATE_ACTIONS.map((action) => (
                        <KuzhambuButton
                            testId="knowledge-graph-extraction-graph-extraction-create-action-button-2"
                            key={action.key}
                            type={action.key === "GRAPH" ? "primary" : "default"}
                            disabled={!canEdit}
                            loading={creatingTaskType === action.key}
                            onClick={() => submitTask(action.key)}
                        >
                            {action.label}
                        </KuzhambuButton>
                    ))}
                </KuzhambuSpace>
            </Card>

            {latestCreatedTask ? (
                <Card className="graph-extraction-create-result" variant="borderless">
                    <KuzhambuSpace orientation="vertical" size={4}>
                        <strong>最近创建任务</strong>
                        <span>任务号：{latestCreatedTask.taskId || "-"}</span>
                        <span>任务类型：{latestCreatedTask.taskType || "-"}</span>
                        <span>状态：{latestCreatedTask.status || "-"}</span>
                        <span>批次号：{latestCreatedTask.batchJobId || "-"}</span>
                        <span>触发来源：{latestCreatedTask.triggerSource || "-"}</span>
                    </KuzhambuSpace>
                </Card>
            ) : null}
        </KuzhambuSpace>
    );
};
