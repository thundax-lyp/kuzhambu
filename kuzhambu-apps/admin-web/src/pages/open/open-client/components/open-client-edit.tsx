import { KeyOutlined } from "@ant-design/icons";
import { Button, Form, Input, Select, Typography } from "antd";
import { useEffect } from "react";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { OpenClientSaveCommand } from "../open-client-service";
import type { OpenClientRecord } from "../open-client-types";
import { OpenClientSecretField } from "./open-client-secret-field";

const { Text } = Typography;
const { TextArea } = Input;

interface OpenClientEditProps {
    open?: boolean;
    client?: OpenClientRecord | null;
    saving?: boolean;
    canEdit?: boolean;
    resetSecretLoading?: boolean;
    onClose: () => void;
    onSave: (request: OpenClientSaveCommand) => void;
    onGenerateSecret: (client: OpenClientRecord) => void;
    onCopySecret: (label: string, value?: string | null) => void;
}

interface OpenClientFormValues {
    id?: string | null;
    name: string;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[];
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const toDateTimeLocalValue = (value?: string | null) => {
    if (!value) {
        return undefined;
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value.slice(0, 16);
    }

    const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60 * 1000);
    return offsetDate.toISOString().slice(0, 16);
};

const toApiDateValue = (value?: string | null) => {
    const normalizedValue = normalizeText(value);
    if (!normalizedValue) {
        return undefined;
    }

    const date = new Date(normalizedValue);
    return Number.isNaN(date.getTime()) ? normalizedValue : date.toISOString();
};

const formatIpWhitelistForForm = (value?: string | null) => {
    const normalizedValue = normalizeText(value);
    if (!normalizedValue) {
        return "";
    }

    try {
        const parsedValue = JSON.parse(normalizedValue);
        if (Array.isArray(parsedValue)) {
            return parsedValue.map(String).join("\n");
        }
    } catch {
        return normalizedValue;
    }

    return normalizedValue;
};

const toIpWhitelistJson = (value?: string | null) => {
    const items = (value || "")
        .split(/\r?\n|,/)
        .map((item) => item.trim())
        .filter(Boolean);
    return items.length > 0 ? JSON.stringify(items) : undefined;
};

const readFormRequest = (values: OpenClientFormValues): OpenClientSaveCommand => {
    return {
        id: values.id,
        name: values.name.trim(),
        ipWhitelist: toIpWhitelistJson(values.ipWhitelist),
        expiredAt: toApiDateValue(values.expiredAt),
        remarks: normalizeText(values.remarks),
        permissions: (values.permissions || [])
            .map((permission) => permission.trim())
            .filter(Boolean)
    };
};

const toFormValues = (client: OpenClientRecord): OpenClientFormValues => {
    return {
        id: client.id,
        name: client.name,
        ipWhitelist: formatIpWhitelistForForm(client.ipWhitelist),
        expiredAt: toDateTimeLocalValue(client.expiredAt),
        remarks: client.remarks,
        permissions: client.permissions || []
    };
};

export const OpenClientEdit = ({
    open,
    client,
    saving,
    canEdit,
    resetSecretLoading,
    onClose,
    onSave,
    onGenerateSecret,
    onCopySecret
}: OpenClientEditProps) => {
    const [form] = Form.useForm<OpenClientFormValues>();
    const editing = Boolean(client);

    useEffect(() => {
        if (!open) {
            return;
        }
        if (client) {
            form.setFieldsValue(toFormValues(client));
            return;
        }
        form.resetFields();
        form.setFieldsValue({ permissions: ["submission:submission:create"] });
    }, [client, form, open]);

    const saveOpenClient = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values));
    };

    const renderApiKey = () => {
        if (!client) {
            return null;
        }

        if (client.apiKey) {
            return (
                <div className="open-client-editor-api-key">
                    <OpenClientSecretField
                        label="API KEY"
                        value={client.apiKey}
                        onCopy={onCopySecret}
                    />
                </div>
            );
        }

        return (
            <div className="open-client-editor-api-key open-client-editor-api-key-empty">
                <Text type="secondary">API KEY 未生成</Text>
                {canEdit ? (
                    <Button
                        icon={<KeyOutlined />}
                        loading={resetSecretLoading}
                        onClick={() => onGenerateSecret(client)}
                    >
                        生成凭据
                    </Button>
                ) : null}
            </div>
        );
    };

    return (
        <SandwishDrawer
            title={editing ? "编辑开放客户端" : "新增开放客户端"}
            open={Boolean(open)}
            size="middle"
            onClose={onClose}
            footer={
                <div className="open-client-editor-footer">
                    <Button onClick={onClose}>取消</Button>
                    <Button type="primary" loading={saving} onClick={saveOpenClient}>
                        保存
                    </Button>
                </div>
            }
        >
            <Form form={form} className="open-client-editor-form" layout="vertical">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                {renderApiKey()}
                <Form.Item
                    name="name"
                    label="第三方主体名称"
                    rules={[{ required: true, message: "请输入第三方主体名称" }]}
                >
                    <Input maxLength={128} placeholder="第三方应用或客户名称" />
                </Form.Item>
                <Form.Item name="ipWhitelist" label="IP 白名单">
                    <TextArea rows={4} placeholder="每行一个 IP 或 CIDR" />
                </Form.Item>
                <Form.Item name="expiredAt" label="过期时间">
                    <Input type="datetime-local" />
                </Form.Item>
                <Form.Item name="permissions" label="权限">
                    <Select mode="tags" tokenSeparators={[",", "\n"]} placeholder="权限码" />
                </Form.Item>
                <Form.Item name="remarks" label="备注">
                    <TextArea rows={3} maxLength={255} />
                </Form.Item>
            </Form>
        </SandwishDrawer>
    );
};
