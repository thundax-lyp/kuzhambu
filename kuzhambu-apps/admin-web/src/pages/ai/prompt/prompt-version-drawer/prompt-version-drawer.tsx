import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Typography } from "antd";
import type { Key } from "react";
import { useMemo, useState } from "react";
import {
    KuzhambuButton,
    KuzhambuDrawer,
    KuzhambuModal,
    KuzhambuTable,
    KuzhambuTag,
    type KuzhambuTableProps
} from "@/components";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import * as service from "@/pages/ai/prompt/prompt-service";
import type { AiPromptTemplateRecord, AiPromptVersionRecord } from "@/pages/ai/prompt/prompt-types";
import "./prompt-version-drawer.css";

interface PromptVersionDrawerProps {
    canEdit: boolean;
    open: boolean;
    template: AiPromptTemplateRecord | null;
    onClose: () => void;
    onChanged: () => void;
}

const EMPTY_JSON_ARRAY = "[]";
const EMPTY_JSON_OBJECT = "{}";
const TEXT_OUTPUT_SCHEMA = '{\n  "type": "text"\n}';

const normalizeJsonText = (value?: string | null, fallback = EMPTY_JSON_OBJECT) => {
    const trimmed = value?.trim();
    return trimmed ? trimmed : fallback;
};

const parseJson = (value: string) => JSON.parse(value) as unknown;

const formatJsonText = (value?: string | null, fallback = EMPTY_JSON_OBJECT) => {
    const normalized = normalizeJsonText(value, fallback);
    try {
        return JSON.stringify(parseJson(normalized), null, 2);
    } catch {
        return normalized;
    }
};

const readPromptOutputStructure = (value?: string | null) => {
    try {
        const parsed = parseJson(normalizeJsonText(value, TEXT_OUTPUT_SCHEMA));
        if (
            parsed != null &&
            typeof parsed === "object" &&
            "type" in parsed &&
            (parsed as { type?: unknown }).type === "text"
        ) {
            return "TEXT";
        }
    } catch {
        return "JSON";
    }
    return "JSON";
};

const formatDate = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const timestamp = Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return value;
    }
    const date = new Date(timestamp);
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${date.getFullYear()}-${month}-${day}`;
};

const versionTitle = (version: AiPromptVersionRecord) => `版本 ${version.versionNo ?? "-"}`;

const versionKey = (version: AiPromptVersionRecord) =>
    version.id || `${version.templateId}-${version.versionNo}`;

const PromptVersionDetail = ({ version }: { version: AiPromptVersionRecord }) => (
    <div className="prompt-version-detail">
        <Typography.Title level={5}>{versionTitle(version)}</Typography.Title>
        <Typography.Text strong>消息模板 JSON</Typography.Text>
        <pre>{formatJsonText(version.messageTemplatesJson, EMPTY_JSON_ARRAY)}</pre>
        <Typography.Text strong>变量快照 JSON</Typography.Text>
        <pre>{formatJsonText(version.variablesSnapshotJson, EMPTY_JSON_ARRAY)}</pre>
        <Typography.Text strong>输出格式</Typography.Text>
        <pre>{readPromptOutputStructure(version.outputSchemaJson)}</pre>
        <Typography.Text strong>输出格式详情 JSON</Typography.Text>
        <pre>{formatJsonText(version.outputSchemaJson, EMPTY_JSON_OBJECT)}</pre>
        <Typography.Text strong>变更说明</Typography.Text>
        <pre>{version.changeSummary || "-"}</pre>
    </div>
);

const PromptVersionCompareModal = ({
    versions,
    onClose
}: {
    versions: AiPromptVersionRecord[];
    onClose: () => void;
}) => (
    <KuzhambuModal
        testId="ai-prompt-prompt-version-compare-modal"
        open={versions.length > 0}
        title="版本对比"
        width={960}
        footer={null}
        onCancel={onClose}
    >
        <div className="prompt-version-compare-grid">
            {versions.map((version) => (
                <PromptVersionDetail key={version.versionNo} version={version} />
            ))}
        </div>
    </KuzhambuModal>
);

export const PromptVersionDrawer = ({
    canEdit,
    open,
    template,
    onClose,
    onChanged
}: PromptVersionDrawerProps) => {
    const { message } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [viewVersion, setViewVersion] = useState<AiPromptVersionRecord | null>(null);
    const [compareVersions, setCompareVersions] = useState<AiPromptVersionRecord[]>([]);
    const [currentVersionNo, setCurrentVersionNo] = useState<number | null>(
        template?.currentVersionNo ?? null
    );
    const templateId = template?.id || null;
    const versionsQuery = useQuery({
        queryKey: ["ai", "prompt", "versions", templateId],
        queryFn: () => service.listPromptVersions(templateId || ""),
        enabled: open && Boolean(templateId),
        retry: false
    });
    const versions = useMemo(() => versionsQuery.data || [], [versionsQuery.data]);
    const selectedVersions = useMemo(() => {
        const selectedKeys = new Set(selectedRowKeys.map(String));
        return versions.filter((version) => selectedKeys.has(String(versionKey(version))));
    }, [selectedRowKeys, versions]);
    const canCompare = Boolean(templateId) && selectedVersions.length === 2;

    const compareMutation = useMutation({
        mutationFn: service.previewPromptVersionCompare,
        onSuccess: setCompareVersions,
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "版本对比失败");
        }
    });
    const rollbackMutation = useMutation({
        mutationFn: service.changePromptVersionRollback,
        onSuccess: async (version) => {
            setCurrentVersionNo(version.versionNo ?? null);
            await queryClient.invalidateQueries({ queryKey: ["ai", "prompt"] });
            await versionsQuery.refetch();
            setSelectedRowKeys([]);
            onChanged();
            message.success("提示词版本已回滚");
        },
        onError: (error) => {
            message.error(error instanceof Error ? error.message : "提示词版本回滚失败");
        }
    });

    const compareSelectedVersions = () => {
        const [leftVersion, rightVersion] = selectedVersions;
        if (!templateId || !leftVersion?.versionNo || !rightVersion?.versionNo) {
            return;
        }
        compareMutation.mutate({
            id: templateId,
            leftVersionNo: leftVersion.versionNo,
            rightVersionNo: rightVersion.versionNo
        });
    };

    const columns: KuzhambuTableProps<AiPromptVersionRecord>["columns"] = [
        { title: "版本号", dataIndex: "versionNo", key: "versionNo", width: 96 },
        {
            title: "状态",
            key: "current",
            width: 96,
            render: (_, record) => {
                const current = record.versionNo === currentVersionNo;
                return (
                    <KuzhambuTag type={current ? "success" : "neutral"}>
                        {current ? "当前" : "历史"}
                    </KuzhambuTag>
                );
            }
        },
        {
            title: "变更说明",
            dataIndex: "changeSummary",
            key: "changeSummary",
            ellipsis: true,
            render: (value?: string | null) => value || "-"
        },
        {
            title: "日期",
            dataIndex: "registeredAt",
            key: "registeredAt",
            width: 120,
            render: formatDate
        },
        {
            key: "actions",
            options: (record) => {
                const current = record.versionNo === currentVersionNo;
                return [
                    { key: "view", text: "查看", onClick: () => setViewVersion(record) },
                    {
                        key: "rollback",
                        text: "回滚",
                        type: "warning",
                        disabled: !canEdit || current,
                        onClick: () =>
                            confirm.danger({
                                title: "回滚版本",
                                message: `确认回滚到版本 ${record.versionNo}？`,
                                okText: "回滚",
                                onConfirm: () =>
                                    rollbackMutation.mutateAsync({
                                        id: templateId || "",
                                        versionNo: record.versionNo || 0
                                    })
                            })
                    }
                ];
            }
        }
    ];

    return (
        <>
            <KuzhambuDrawer
                testId="ai-prompt-prompt-version-drawer"
                open={open}
                title="提示词版本"
                size="large"
                onClose={onClose}
            >
                <div className="prompt-version-toolbar">
                    <KuzhambuButton
                        testId="ai-prompt-prompt-version-compare-button"
                        type="primary"
                        disabled={!canCompare}
                        loading={compareMutation.isPending}
                        onClick={compareSelectedVersions}
                    >
                        对比
                    </KuzhambuButton>
                </div>
                <KuzhambuTable<AiPromptVersionRecord>
                    ariaLabel="提示词版本列表"
                    rowKey={versionKey}
                    columns={columns}
                    dataSource={versions}
                    loading={versionsQuery.isFetching}
                    pagination={false}
                    size="small"
                    rowSelection={{
                        selectedRowKeys,
                        onChange: (nextKeys) => setSelectedRowKeys(nextKeys),
                        getCheckboxProps: (record) => ({
                            disabled:
                                selectedRowKeys.length >= 2 &&
                                !selectedRowKeys.includes(versionKey(record))
                        })
                    }}
                />
            </KuzhambuDrawer>
            <KuzhambuDrawer
                testId="ai-prompt-prompt-version-detail-drawer"
                open={Boolean(viewVersion)}
                title={viewVersion ? versionTitle(viewVersion) : "版本详情"}
                size="large"
                onClose={() => setViewVersion(null)}
            >
                {viewVersion ? <PromptVersionDetail version={viewVersion} /> : null}
            </KuzhambuDrawer>
            <PromptVersionCompareModal
                versions={compareVersions}
                onClose={() => setCompareVersions([])}
            />
        </>
    );
};
