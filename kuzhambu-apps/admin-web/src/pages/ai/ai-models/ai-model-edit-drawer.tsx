import { App, Form, Input } from "antd";
import { resolveTextAreaAutoSize } from "@/components/form/text-area-auto-size";
import {
    KuzhambuDrawer,
    KuzhambuForm,
    KuzhambuFormItem,
    KuzhambuSwitch,
    KuzhambuSelect
} from "@/components";

import type { AiModelChangeCommand } from "./ai-models-service";
import type { AiModelRecord } from "./ai-models-types";

import {
    API_SOURCE_OPTIONS,
    DEFAULT_MODEL_PARAMS,
    MODEL_CAPABILITY_OPTIONS,
    normalizeJsonText,
    readApiSourceMeta,
    readCapabilityMeta
} from "./ai-models-metadata";

type AiModelFormValues = AiModelChangeCommand;

const createEmptyForm = (): AiModelFormValues => ({
    apiSource: API_SOURCE_OPTIONS[0],
    baseUrl: "",
    apiKey: "",
    modelName: "",
    displayName: "",
    capabilities: [],
    defaultParamsJson: DEFAULT_MODEL_PARAMS,
    description: "",
    enabled: true
});

const toFormValues = (model: AiModelRecord | null): AiModelFormValues => {
    if (!model) {
        return createEmptyForm();
    }

    return {
        id: model.id,
        apiSource: model.apiSource,
        baseUrl: model.baseUrl || "",
        apiKey: "",
        modelName: model.modelName,
        displayName: model.displayName || "",
        capabilities: model.capabilities || [],
        defaultParamsJson: normalizeJsonText(model.defaultParamsJson),
        description: model.description || "",
        enabled: model.enabled
    };
};

interface AiModelEditDrawerProps {
    canEdit: boolean;
    model: AiModelRecord | null;
    open: boolean;
    saving: boolean;
    onClose: () => void;
    onSave: (command: AiModelChangeCommand) => void;
}

const omitBlankApiKey = (command: AiModelChangeCommand): AiModelChangeCommand => {
    if (!command.apiKey?.trim()) {
        const sanitized = { ...command };
        delete sanitized.apiKey;
        return sanitized;
    }
    return command;
};

export const AiModelEditDrawer = ({
    canEdit,
    model,
    open,
    saving,
    onClose,
    onSave
}: AiModelEditDrawerProps) => {
    const formKey = `${open ? "open" : "closed"}-${model?.id ?? "create"}`;

    return (
        <AiModelEditDrawerForm
            key={formKey}
            canEdit={canEdit}
            model={model}
            open={open}
            saving={saving}
            onClose={onClose}
            onSave={onSave}
        />
    );
};

const AiModelEditDrawerForm = ({
    canEdit,
    model,
    open,
    saving,
    onClose,
    onSave
}: AiModelEditDrawerProps) => {
    const { message: messageApi } = App.useApp();
    const [form] = Form.useForm<AiModelFormValues>();
    const initialValues = toFormValues(model);

    const submitForm = () => {
        form.validateFields().then((values) => {
            if (!values.displayName?.trim()) {
                messageApi.warning("请输入模型名称");
                return;
            }
            if (!values.modelName.trim()) {
                messageApi.warning("请输入模型标识");
                return;
            }
            if (!values.apiSource) {
                messageApi.warning("请选择供应商");
                return;
            }
            if (!values.baseUrl.trim()) {
                messageApi.warning("请输入服务地址");
                return;
            }
            try {
                JSON.parse(normalizeJsonText(values.defaultParamsJson));
            } catch {
                messageApi.warning("请输入合法 JSON");
                return;
            }

            const command = {
                ...values,
                id: model?.id || values.id || null,
                baseUrl: values.baseUrl.trim(),
                modelName: values.modelName.trim(),
                defaultParamsJson: normalizeJsonText(values.defaultParamsJson),
                displayName: values.displayName?.trim() || null,
                description: values.description?.trim() || null,
                capabilities: values.capabilities || []
            };
            onSave(model ? omitBlankApiKey(command) : command);
        });
    };

    const formProps = {
        className: "ai-models-form",
        colon: false,
        component: "div" as const
    };

    return (
        <KuzhambuDrawer
            testId="ai-ai-models-ai-model-edit-drawer"
            open={open}
            title={model ? "编辑模型" : "新增模型"}
            size="large"
            onClose={onClose}
            footerActions={[
                { testId: "ai-models-cancel-button", title: "取消", action: onClose },
                {
                    testId: "ai-models-save-button",
                    title: "保存",
                    type: "primary",
                    disabled: !canEdit,
                    loading: saving,
                    action: submitForm
                }
            ]}
        >
            <KuzhambuForm<AiModelFormValues>
                {...formProps}
                form={form}
                initialValues={initialValues}
            >
                <KuzhambuFormItem name="displayName" label="模型名称">
                    <Input aria-label="AI模型名称" placeholder="对后台用户展示的名称" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="apiSource" label="供应商">
                    <KuzhambuSelect
                        aria-label="AI模型供应商"
                        options={API_SOURCE_OPTIONS.map((value) => ({
                            label: readApiSourceMeta(value).label,
                            value
                        }))}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="baseUrl" label="服务地址" layoutSize="large">
                    <Input aria-label="AI模型服务地址" placeholder="https://api.example.com/v1" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="apiKey" label="API 密钥" layoutSize="large">
                    <Input.Password
                        aria-label="AI模型API密钥"
                        placeholder={model?.apiKeyConfigured ? "已配置，留空则不更新" : ""}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="modelName" label="模型标识" layoutSize="large">
                    <Input aria-label="AI模型标识" placeholder="gpt-4o" />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="capabilities" label="能力" layoutSize="middle">
                    <KuzhambuSelect
                        aria-label="AI模型能力"
                        mode="multiple"
                        options={MODEL_CAPABILITY_OPTIONS.map((value) => ({
                            label: readCapabilityMeta(value).label,
                            value
                        }))}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem
                    name="enabled"
                    label="状态"
                    layoutSize="middle"
                    valuePropName="checked"
                >
                    <KuzhambuSwitch
                        aria-label="AI模型状态"
                        checkedChildren="启用"
                        unCheckedChildren="禁用"
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="defaultParamsJson" label="默认参数 JSON" layoutSize="large">
                    <Input.TextArea
                        aria-label="AI模型默认参数JSON"
                        autoSize={resolveTextAreaAutoSize({ minRows: 6, maxRows: 10 })}
                    />
                </KuzhambuFormItem>
                <KuzhambuFormItem name="description" label="说明" layoutSize="large">
                    <Input.TextArea
                        aria-label="AI模型说明"
                        autoSize={resolveTextAreaAutoSize({ minRows: 3, maxRows: 6 })}
                    />
                </KuzhambuFormItem>
            </KuzhambuForm>
        </KuzhambuDrawer>
    );
};
