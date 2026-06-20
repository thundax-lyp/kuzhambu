import { Alert, Button, Descriptions, Empty, List, Space, Tag, Typography } from "antd";
import type {
    WangqiContentVersionRecord,
    WangqiDocumentRecord,
    WangqiVersionSnapshot
} from "../wangqi-types";

const { Text } = Typography;

const compareFields: Array<{
    key: keyof WangqiVersionSnapshot;
    label: string;
}> = [
    { key: "title", label: "标题" },
    { key: "summary", label: "摘要" },
    { key: "contentFormat", label: "正文格式" },
    { key: "content", label: "正文" },
    { key: "documentTime", label: "文档时间" },
    { key: "storageObjectId", label: "原始文件对象 ID" },
    { key: "visibility", label: "可见性" }
];

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const readSnapshot = (
    version?: WangqiContentVersionRecord | null
): WangqiVersionSnapshot | null => {
    if (!version?.snapshotJson) {
        return null;
    }
    try {
        return JSON.parse(version.snapshotJson) as WangqiVersionSnapshot;
    } catch {
        return null;
    }
};

const readCurrentValue = (
    document: WangqiDocumentRecord | null | undefined,
    key: keyof WangqiVersionSnapshot
) => {
    if (!document) {
        return undefined;
    }
    return document[key as keyof WangqiDocumentRecord];
};

const formatValue = (value: unknown) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    return String(value);
};

export interface WangqiVersionHistoryPanelProps {
    currentDocument?: WangqiDocumentRecord | null;
    detailLoading?: boolean;
    listLoading?: boolean;
    resetting?: boolean;
    selectedVersion?: WangqiContentVersionRecord | null;
    versions: WangqiContentVersionRecord[];
    onResetVersion: (version: WangqiContentVersionRecord) => void;
    onSelectVersion: (version: WangqiContentVersionRecord) => void;
}

export const WangqiVersionHistoryPanel = ({
    currentDocument,
    detailLoading = false,
    listLoading = false,
    resetting = false,
    selectedVersion,
    versions,
    onResetVersion,
    onSelectVersion
}: WangqiVersionHistoryPanelProps) => {
    const snapshot = readSnapshot(selectedVersion);

    return (
        <section className="wangqi-version-history-panel" aria-label="王圻版本历史面板">
            <div className="wangqi-version-history-panel-grid">
                <List<WangqiContentVersionRecord>
                    className="wangqi-version-history-list"
                    aria-label="王圻版本历史列表"
                    loading={listLoading}
                    dataSource={versions}
                    locale={{ emptyText: "暂无版本历史" }}
                    renderItem={(version) => (
                        <List.Item
                            actions={[
                                <Button
                                    key="view"
                                    type="link"
                                    aria-label={`查看王圻版本 ${version.versionNo ?? version.id}`}
                                    onClick={() => onSelectVersion(version)}
                                >
                                    查看
                                </Button>
                            ]}
                        >
                            <List.Item.Meta
                                title={
                                    <Space wrap>
                                        <Text strong>版本 {version.versionNo ?? "-"}</Text>
                                        <Tag>{version.changeType || "UNKNOWN"}</Tag>
                                    </Space>
                                }
                                description={
                                    <Space direction="vertical" size={2}>
                                        <Text>{version.changeSummary || "未填写变更说明"}</Text>
                                        <Text type="secondary">
                                            {formatDateTime(version.versionedAt)}
                                        </Text>
                                    </Space>
                                }
                            />
                        </List.Item>
                    )}
                />
                <div className="wangqi-version-history-detail" aria-busy={detailLoading}>
                    {selectedVersion ? (
                        <Space
                            direction="vertical"
                            size="middle"
                            className="wangqi-version-history-detail-stack"
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
                                    className="wangqi-version-compare"
                                    size="small"
                                    column={1}
                                    bordered
                                >
                                    {compareFields.map((field) => {
                                        const currentValue = readCurrentValue(
                                            currentDocument,
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
                                                <Space direction="vertical" size={2}>
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
                                <Alert type="warning" showIcon message="版本快照为空或无法解析" />
                            )}
                            <Button
                                danger
                                aria-label={`恢复王圻版本 ${selectedVersion.versionNo ?? selectedVersion.id}`}
                                loading={resetting}
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
