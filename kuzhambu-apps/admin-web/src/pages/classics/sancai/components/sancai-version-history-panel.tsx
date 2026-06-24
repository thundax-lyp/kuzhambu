import { Alert, Button, Descriptions, Empty, Space, Tag, Typography } from "antd";
import { KuzhambuList, KuzhambuListItem, KuzhambuListMeta } from "@/components/kuzhambu-list";
import type {
    SancaiContentVersionRecord,
    SancaiEntryRecord,
    SancaiVersionSnapshot
} from "../sancai-types";

const { Text } = Typography;

const compareFields: Array<{
    key: keyof SancaiVersionSnapshot;
    label: string;
}> = [
    { key: "volumeId", label: "卷目 ID" },
    { key: "title", label: "标题" },
    { key: "originalText", label: "原文" },
    { key: "translationText", label: "译文" },
    { key: "summary", label: "摘要" },
    { key: "lifecycleStatus", label: "生命周期" },
    { key: "visibility", label: "可见性" },
    { key: "translationStatus", label: "翻译状态" },
    { key: "imageStatus", label: "配图状态" },
    { key: "visualAssetStatus", label: "视觉资产状态" },
    { key: "refinementStatus", label: "精修状态" },
    { key: "priority", label: "历史排序值" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const readSnapshot = (
    version?: SancaiContentVersionRecord | null
): SancaiVersionSnapshot | null => {
    if (!version?.snapshotJson) {
        return null;
    }
    try {
        return JSON.parse(version.snapshotJson) as SancaiVersionSnapshot;
    } catch {
        return null;
    }
};

const readCurrentValue = (
    entry: SancaiEntryRecord | null | undefined,
    key: keyof SancaiVersionSnapshot
) => {
    if (!entry) {
        return undefined;
    }
    return entry[key as keyof SancaiEntryRecord];
};

const formatValue = (value: unknown) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    return String(value);
};

export interface SancaiVersionHistoryPanelProps {
    currentEntry?: SancaiEntryRecord | null;
    detailLoading?: boolean;
    listLoading?: boolean;
    resetting?: boolean;
    selectedVersion?: SancaiContentVersionRecord | null;
    versions: SancaiContentVersionRecord[];
    onResetVersion: (version: SancaiContentVersionRecord) => void;
    onSelectVersion: (version: SancaiContentVersionRecord) => void;
}

export const SancaiVersionHistoryPanel = ({
    currentEntry,
    detailLoading = false,
    listLoading = false,
    resetting = false,
    selectedVersion,
    versions,
    onResetVersion,
    onSelectVersion
}: SancaiVersionHistoryPanelProps) => {
    const snapshot = readSnapshot(selectedVersion);

    return (
        <section className="sancai-version-history-panel" aria-label="三才图会版本历史面板">
            <div className="sancai-version-history-panel-grid">
                <KuzhambuList<SancaiContentVersionRecord>
                    as="ol"
                    className="sancai-version-history-list"
                    aria-label="三才图会版本历史列表"
                    loading={listLoading}
                    dataSource={versions}
                    empty="暂无版本历史"
                    renderItem={(version) => (
                        <KuzhambuListItem
                            className="sancai-version-history-list-item"
                            key={version.id}
                            actions={[
                                <Button
                                    key="view"
                                    type="link"
                                    aria-label={`查看三才图会版本 ${version.versionNo ?? version.id}`}
                                    onClick={() => onSelectVersion(version)}
                                >
                                    查看
                                </Button>
                            ]}
                        >
                            <KuzhambuListMeta
                                title={
                                    <Space wrap>
                                        <Text strong>版本 {version.versionNo ?? "-"}</Text>
                                        <Tag>{version.changeType || "UNKNOWN"}</Tag>
                                    </Space>
                                }
                                description={
                                    <Space orientation="vertical" size={2}>
                                        <Text>{version.changeSummary || "未填写变更说明"}</Text>
                                        <Text type="secondary">
                                            {formatDateTime(version.versionedAt)}
                                        </Text>
                                    </Space>
                                }
                            />
                        </KuzhambuListItem>
                    )}
                />
                <div className="sancai-version-history-detail" aria-busy={detailLoading}>
                    {selectedVersion ? (
                        <Space
                            orientation="vertical"
                            size="middle"
                            className="sancai-version-history-detail-stack"
                        >
                            <Descriptions size="small" column={2} bordered>
                                <Descriptions.Item label="版本号">
                                    {selectedVersion.versionNo ?? "-"}
                                </Descriptions.Item>
                                <Descriptions.Item label="变更类型">
                                    {selectedVersion.changeType || "-"}
                                </Descriptions.Item>
                                <Descriptions.Item label="版本时间">
                                    {formatDateTime(selectedVersion.versionedAt)}
                                </Descriptions.Item>
                                <Descriptions.Item label="变更说明">
                                    {selectedVersion.changeSummary || "-"}
                                </Descriptions.Item>
                            </Descriptions>
                            {snapshot ? (
                                <Descriptions
                                    className="sancai-version-compare"
                                    size="small"
                                    column={1}
                                    bordered
                                >
                                    {compareFields.map((field) => {
                                        const currentValue = readCurrentValue(
                                            currentEntry,
                                            field.key
                                        );
                                        const historyValue = snapshot[field.key];
                                        const changed =
                                            formatValue(currentValue) !== formatValue(historyValue);
                                        return (
                                            <Descriptions.Item
                                                key={field.key}
                                                label={field.label}
                                                className={changed ? "is-changed" : undefined}
                                            >
                                                <Space orientation="vertical" size={2}>
                                                    <Text>当前：{formatValue(currentValue)}</Text>
                                                    <Text type={changed ? "warning" : "secondary"}>
                                                        历史：{formatValue(historyValue)}
                                                    </Text>
                                                </Space>
                                            </Descriptions.Item>
                                        );
                                    })}
                                </Descriptions>
                            ) : (
                                <Alert type="warning" showIcon title="版本快照为空或无法解析" />
                            )}
                            <Button
                                danger
                                aria-label={`恢复三才图会版本 ${selectedVersion.versionNo ?? selectedVersion.id}`}
                                loading={resetting}
                                disabled={!snapshot}
                                onClick={() => onResetVersion(selectedVersion)}
                            >
                                恢复此版本
                            </Button>
                        </Space>
                    ) : (
                        <Empty
                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                            description="请选择版本查看对比"
                        />
                    )}
                </div>
            </div>
        </section>
    );
};
