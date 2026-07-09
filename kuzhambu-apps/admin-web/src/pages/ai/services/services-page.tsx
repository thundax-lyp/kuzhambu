import { EditOutlined, ReloadOutlined, SaveOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Card, Descriptions, Form, Input, Select, Switch, Tooltip } from "antd";
import { useEffect, useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuPage } from "@/components/kuzhambu-page";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type { KuzhambuTagType } from "@/components/kuzhambu-tag";
import * as service from "./services-service";
import type { AiServiceConfigChangeCommand } from "./services-service";
import type { AiServiceConfigRecord, AiServiceRole } from "./services-types";
import "./services-page.css";

type ServiceFormValues = AiServiceConfigChangeCommand;

const SERVICE_ROLES = ["PRIMARY", "BACKUP"] as const;
const API_SOURCE_OPTIONS = ["OPENAI", "AZURE_OPENAI", "LOCAL", "OTHER"];
const STATUS_OPTIONS = ["AVAILABLE", "UNAVAILABLE", "CHECKING"];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

const booleanText = (value: boolean) => (value ? "是" : "否");

const statusTone = (status?: string | null): KuzhambuTagType => {
    if (status === "AVAILABLE") {
        return "success";
    }
    if (status === "CHECKING") {
        return "warning";
    }
    return "danger";
};

export const ServicesPage = () => {
    const { message } = App.useApp();
    const queryClient = useQueryClient();
    const [form] = Form.useForm<ServiceFormValues>();
    const canViewConfig = hasPermission("ai:config:view");
    const canEditConfig = hasPermission("ai:config:edit");
    const [editorOpen, setEditorOpen] = useState(false);

    const servicesQuery = useQuery({
        queryKey: ["ai", "services"],
        queryFn: service.listGovernanceServices,
        enabled: canViewConfig,
        retry: false
    });

    const serviceByRole = useMemo(() => {
        return new Map((servicesQuery.data || []).map((record) => [record.serviceRole, record]));
    }, [servicesQuery.data]);

    const saveMutation = useMutation({
        mutationFn: service.changeServiceConfig,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["ai", "services"] });
            setEditorOpen(false);
            form.resetFields();
            message.success("服务配置已保存");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "服务配置保存失败");
        }
    });

    useEffect(() => {
        if (servicesQuery.isError) {
            const error = servicesQuery.error;
            message.error(error instanceof Error ? error.message : "服务配置加载失败");
        }
    }, [message, servicesQuery.error, servicesQuery.isError]);

    const openEditor = (role: AiServiceRole, record?: AiServiceConfigRecord) => {
        form.setFieldsValue({
            serviceId: record?.serviceId || null,
            serviceRole: record?.serviceRole || role,
            apiSource: record?.apiSource || API_SOURCE_OPTIONS[0],
            baseUrl: record?.baseUrl || "",
            encryptedApiKey: null,
            enabled: record?.enabled ?? true,
            status: record?.status || "UNAVAILABLE"
        });
        setEditorOpen(true);
    };

    const submitForm = async () => {
        const values = await form.validateFields();
        const { encryptedApiKey, ...restValues } = values;
        const trimmedApiKey = encryptedApiKey?.trim();
        await saveMutation.mutateAsync({
            ...restValues,
            ...(trimmedApiKey ? { encryptedApiKey: trimmedApiKey } : {})
        });
    };

    return (
        <KuzhambuPage
            className="services-page"
            eyebrow="AI"
            title="AI 服务配置"
            description="管理主服务和备用服务连接状态"
            actions={
                <Tooltip title="刷新">
                    <Button
                        aria-label="刷新"
                        icon={<ReloadOutlined />}
                        loading={servicesQuery.isFetching}
                        onClick={() => void servicesQuery.refetch()}
                    />
                </Tooltip>
            }
        >
            <section className="services-grid">
                {SERVICE_ROLES.map((role) => {
                    const record = serviceByRole.get(role);
                    return (
                        <Card
                            key={role}
                            className="services-card"
                            title={role === "PRIMARY" ? "PRIMARY 主服务" : "BACKUP 备用服务"}
                            extra={
                                <Button
                                    icon={<EditOutlined />}
                                    disabled={!canEditConfig}
                                    onClick={() => openEditor(role, record)}
                                >
                                    {record ? "编辑" : "配置"}
                                </Button>
                            }
                        >
                            {record ? (
                                <Descriptions column={1} size="small">
                                    <Descriptions.Item label="serviceRole">
                                        {record.serviceRole}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="apiSource">
                                        {record.apiSource}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="baseUrl">
                                        {record.baseUrl}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="apiKeyConfigured">
                                        {booleanText(record.apiKeyConfigured)}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="enabled">
                                        {booleanText(record.enabled)}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="status">
                                        <KuzhambuTag type={statusTone(record.status)}>
                                            {record.status}
                                        </KuzhambuTag>
                                    </Descriptions.Item>
                                    <Descriptions.Item label="lastCheckedAt">
                                        {formatDateTime(record.lastCheckedAt)}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="configuredAt">
                                        {formatDateTime(record.configuredAt)}
                                    </Descriptions.Item>
                                </Descriptions>
                            ) : (
                                <div className="services-empty">暂无配置</div>
                            )}
                        </Card>
                    );
                })}
            </section>

            <KuzhambuDrawer
                open={editorOpen}
                title="编辑 AI 服务"
                size="middle"
                onClose={() => {
                    setEditorOpen(false);
                    form.resetFields();
                }}
                footer={
                    <KuzhambuSpace>
                        <Button
                            onClick={() => {
                                setEditorOpen(false);
                                form.resetFields();
                            }}
                        >
                            取消
                        </Button>
                        <Button
                            type="primary"
                            icon={<SaveOutlined />}
                            disabled={!canEditConfig}
                            loading={saveMutation.isPending}
                            onClick={() => void submitForm()}
                        >
                            保存
                        </Button>
                    </KuzhambuSpace>
                }
            >
                <Form form={form} layout="vertical" className="services-form">
                    <Form.Item name="serviceId" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item label="serviceRole" name="serviceRole">
                        <Input readOnly />
                    </Form.Item>
                    <Form.Item
                        label="apiSource"
                        name="apiSource"
                        rules={[{ required: true, message: "请选择 API 来源" }]}
                    >
                        <Select
                            options={API_SOURCE_OPTIONS.map((value) => ({ label: value, value }))}
                        />
                    </Form.Item>
                    <Form.Item
                        label="baseUrl"
                        name="baseUrl"
                        rules={[{ required: true, message: "请输入服务地址" }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item label="encryptedApiKey" name="encryptedApiKey">
                        <Input.Password placeholder="留空表示不更新密钥" />
                    </Form.Item>
                    <Form.Item label="enabled" name="enabled" valuePropName="checked">
                        <Switch checkedChildren="启用" unCheckedChildren="停用" />
                    </Form.Item>
                    <Form.Item label="status" name="status">
                        <Select
                            options={STATUS_OPTIONS.map((value) => ({ label: value, value }))}
                        />
                    </Form.Item>
                </Form>
            </KuzhambuDrawer>
        </KuzhambuPage>
    );
};
