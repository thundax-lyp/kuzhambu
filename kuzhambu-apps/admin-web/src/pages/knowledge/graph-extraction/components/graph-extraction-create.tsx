import { Button, Card, Form, Input, InputNumber, Space } from "antd";
import type {
    GraphExtractionCreateCommand,
    GraphExtractionTaskRecord,
    GraphExtractionTaskType
} from "../graph-extraction-types";

const { TextArea } = Input;

interface GraphExtractionCreateProps {
    canEdit?: boolean;
    creatingTaskType?: GraphExtractionTaskType | null;
    latestCreatedTask?: GraphExtractionTaskRecord | null;
    onCreate: (request: GraphExtractionCreateCommand) => void;
}

interface GraphExtractionCreateFormValues {
    inputPayloadJson: string;
    locale?: string;
    modelId: number;
    modelName: string;
    promptMessagesJson: string;
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
    onCreate
}: GraphExtractionCreateProps) => {
    const [form] = Form.useForm<GraphExtractionCreateFormValues>();

    const submitTask = async (taskType: GraphExtractionTaskType) => {
        const values = await form.validateFields();
        onCreate(toCreateRequest(taskType, values));
    };

    return (
        <Space orientation="vertical" size={16} className="graph-extraction-create">
            <Card className="graph-extraction-create-card" variant="borderless">
                <Form<GraphExtractionCreateFormValues>
                    form={form}
                    layout="vertical"
                    initialValues={{
                        locale: "zh-CN",
                        modelId: 1
                    }}
                >
                    <div className="graph-extraction-create-grid">
                        <Form.Item
                            name="sourceContentType"
                            label="来源内容类型"
                            rules={[{ required: true, message: "请输入来源内容类型" }]}
                        >
                            <Input placeholder="例如：SANCAI_ENTRY" />
                        </Form.Item>
                        <Form.Item
                            name="sourceContentId"
                            label="来源内容 ID"
                            rules={[{ required: true, message: "请输入来源内容 ID" }]}
                        >
                            <InputNumber min={1} precision={0} style={{ width: "100%" }} />
                        </Form.Item>
                        <Form.Item name="scopeType" label="作用域类型">
                            <Input placeholder="例如：CLASSICS_ENTRY" />
                        </Form.Item>
                        <Form.Item name="locale" label="语言">
                            <Input placeholder="例如：zh-CN" />
                        </Form.Item>
                        <Form.Item
                            name="modelId"
                            label="模型 ID"
                            rules={[{ required: true, message: "请输入模型 ID" }]}
                        >
                            <InputNumber min={1} precision={0} style={{ width: "100%" }} />
                        </Form.Item>
                        <Form.Item
                            name="modelName"
                            label="模型名"
                            rules={[{ required: true, message: "请输入模型名" }]}
                        >
                            <Input placeholder="例如：gpt-5.5" />
                        </Form.Item>
                    </div>

                    <Form.Item name="scopeJson" label="作用域 JSON">
                        <TextArea rows={3} placeholder='例如：{"entryId":1001}' />
                    </Form.Item>
                    <Form.Item
                        name="promptMessagesJson"
                        label="Prompt Messages JSON"
                        rules={[{ required: true, message: "请输入 Prompt Messages JSON" }]}
                    >
                        <TextArea
                            rows={4}
                            placeholder='例如：[{"role":"system","content":"extract"}]'
                        />
                    </Form.Item>
                    <Form.Item
                        name="inputPayloadJson"
                        label="输入 Payload JSON"
                        rules={[{ required: true, message: "请输入输入 Payload JSON" }]}
                    >
                        <TextArea rows={5} placeholder='例如：{"content":"待抽取正文"}' />
                    </Form.Item>

                    <Space wrap>
                        {CREATE_ACTIONS.map((action) => (
                            <Button
                                key={action.key}
                                type={action.key === "GRAPH" ? "primary" : "default"}
                                disabled={!canEdit}
                                loading={creatingTaskType === action.key}
                                onClick={() => submitTask(action.key)}
                            >
                                {action.label}
                            </Button>
                        ))}
                    </Space>
                </Form>
            </Card>

            {latestCreatedTask ? (
                <Card className="graph-extraction-create-result" variant="borderless">
                    <Space orientation="vertical" size={4}>
                        <strong>最近创建任务</strong>
                        <span>任务号：{latestCreatedTask.taskId || "-"}</span>
                        <span>任务类型：{latestCreatedTask.taskType || "-"}</span>
                        <span>状态：{latestCreatedTask.status || "-"}</span>
                    </Space>
                </Card>
            ) : null}
        </Space>
    );
};
