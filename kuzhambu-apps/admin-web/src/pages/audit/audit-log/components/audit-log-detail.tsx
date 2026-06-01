import { Avatar, Descriptions, Empty, Space, Typography } from "antd";
import { ADMIN_API_BASE_URL } from "@/api/http";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import { KuzhambuTag } from "@/components/kuzhambu-tag";
import type {
    AuditFieldRecord,
    AuditLogDetailRecord,
    AuditSnapshotFieldRecord,
    AuditSnapshotRecord
} from "../audit-log-types";

const { Paragraph, Text } = Typography;

const ADMIN_OPERATOR_TYPE = "USER";

interface AuditLogDetailProps {
    accessToken: string | null;
    auditLog?: AuditLogDetailRecord | null;
    error?: boolean;
    loading?: boolean;
    open: boolean;
    onClose: () => void;
}

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
};

const getInitials = (value?: string | null) => {
    const normalizedValue = value?.trim() || "U";
    return Array.from(normalizedValue.replace(/\s+/g, "")).slice(0, 2).join("");
};

const readOperatorName = (log: AuditLogDetailRecord) => {
    return log.operatorName || log.operatorId || "-";
};

const readOperatorUser = (log: AuditLogDetailRecord) => {
    const name = readOperatorName(log);
    const avatarUrl =
        log.operatorType === ADMIN_OPERATOR_TYPE && log.operatorId
            ? `${ADMIN_API_BASE_URL}/sys/user/avatar?id=${encodeURIComponent(log.operatorId)}`
            : undefined;
    return { avatarUrl, name };
};

const renderOperator = (log: AuditLogDetailRecord, accessToken: string | null) => {
    const user = readOperatorUser(log);
    const avatarUrl = toAuthenticatedResourceUrl(user.avatarUrl, accessToken);
    return (
        <Space size={8} className="audit-log-operator-cell">
            <Avatar size={28} src={avatarUrl}>
                {getInitials(user.name)}
            </Avatar>
            <span>{user.name}</span>
        </Space>
    );
};

const renderChangedFields = (fields?: AuditFieldRecord[] | null) => {
    if (!fields?.length) {
        return <Text type="secondary">无字段变更</Text>;
    }

    return (
        <div className="audit-log-field-list">
            {fields.map((field) => (
                <div key={field.fieldName || field.fieldLabel} className="audit-log-field-row">
                    <Text strong>{field.fieldLabel || field.fieldName}</Text>
                    <div className="audit-log-field-values">
                        <Text type="secondary">{field.beforeDisplayValue || "-"}</Text>
                        <span>→</span>
                        <Text>{field.afterDisplayValue || "-"}</Text>
                    </div>
                </div>
            ))}
        </div>
    );
};

const snapshotFieldKey = (field: Pick<AuditSnapshotFieldRecord, "fieldName" | "fieldLabel">) => {
    return field.fieldName || field.fieldLabel || "";
};

const snapshotFieldValue = (field?: AuditSnapshotFieldRecord | null) => {
    return field?.displayValue || "-";
};

const changedFieldKeys = (fields?: AuditFieldRecord[] | null) => {
    const keys = new Set<string>();
    (fields || []).forEach((field) => {
        if (field.fieldName) {
            keys.add(field.fieldName);
        }
        if (field.fieldLabel) {
            keys.add(field.fieldLabel);
        }
    });
    return keys;
};

const snapshotFieldMap = (snapshot?: AuditSnapshotRecord | null) => {
    const fields = new Map<string, AuditSnapshotFieldRecord>();
    (snapshot?.fields || []).forEach((field) => {
        const key = snapshotFieldKey(field);
        if (key) {
            fields.set(key, field);
        }
    });
    return fields;
};

const snapshotFieldKeys = (
    beforeSnapshot?: AuditSnapshotRecord | null,
    afterSnapshot?: AuditSnapshotRecord | null
) => {
    const keys: string[] = [];
    [...(beforeSnapshot?.fields || []), ...(afterSnapshot?.fields || [])].forEach((field) => {
        const key = snapshotFieldKey(field);
        if (key && !keys.includes(key)) {
            keys.push(key);
        }
    });
    return keys;
};

const renderSnapshotCompare = (
    beforeSnapshot?: AuditSnapshotRecord | null,
    afterSnapshot?: AuditSnapshotRecord | null,
    fields?: AuditFieldRecord[] | null
) => {
    const keys = snapshotFieldKeys(beforeSnapshot, afterSnapshot);
    if (!keys.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无快照" />;
    }
    const beforeFields = snapshotFieldMap(beforeSnapshot);
    const afterFields = snapshotFieldMap(afterSnapshot);
    const changedKeys = changedFieldKeys(fields);

    return (
        <div className="audit-log-snapshot-compare">
            <div className="audit-log-snapshot-head">
                <Text type="secondary">字段</Text>
                <Text type="secondary">变更前</Text>
                <Text type="secondary">变更后</Text>
            </div>
            {keys.map((key) => {
                const beforeField = beforeFields.get(key);
                const afterField = afterFields.get(key);
                const beforeValue = snapshotFieldValue(beforeField);
                const afterValue = snapshotFieldValue(afterField);
                const changed = changedKeys.has(key) || beforeValue !== afterValue;
                return (
                    <div
                        key={key}
                        className={
                            changed
                                ? "audit-log-snapshot-row audit-log-snapshot-row-changed"
                                : "audit-log-snapshot-row"
                        }
                    >
                        <div className="audit-log-snapshot-field">
                            <Text strong={changed}>
                                {beforeField?.fieldLabel || afterField?.fieldLabel || key}
                            </Text>
                            {changed ? <KuzhambuTag type="warning">已变更</KuzhambuTag> : null}
                        </div>
                        <Text type="secondary">{beforeValue}</Text>
                        <Text strong={changed}>{afterValue}</Text>
                    </div>
                );
            })}
        </div>
    );
};

export const AuditLogDetail = ({
    accessToken,
    auditLog,
    error = false,
    loading = false,
    open,
    onClose
}: AuditLogDetailProps) => (
    <SandwishDrawer title="审计详情" open={open} size="large" loading={loading} onClose={onClose}>
        {auditLog ? (
            <div className="audit-log-detail">
                <Descriptions column={2} size="small" bordered>
                    <Descriptions.Item label="对象">
                        {auditLog.objectDisplayName || auditLog.objectId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="对象类型">
                        {auditLog.objectTypeLabel || auditLog.objectType || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="动作">
                        {auditLog.actionLabel || auditLog.action || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="版本">{auditLog.version ?? "-"}</Descriptions.Item>
                    <Descriptions.Item label="操作者">
                        {renderOperator(auditLog, accessToken)}
                    </Descriptions.Item>
                    <Descriptions.Item label="操作者类型">
                        {auditLog.operatorTypeLabel || auditLog.operatorType || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="来源">{auditLog.source || "-"}</Descriptions.Item>
                    <Descriptions.Item label="远端地址">
                        {auditLog.remoteAddr || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="请求 ID">
                        {auditLog.requestId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="链路 ID">{auditLog.traceId || "-"}</Descriptions.Item>
                    <Descriptions.Item label="发生时间">
                        {formatDateTime(auditLog.occurredAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="幂等键">
                        {auditLog.idempotencyKey || "-"}
                    </Descriptions.Item>
                </Descriptions>

                <section>
                    <Text type="secondary">摘要</Text>
                    <Paragraph>{auditLog.summary || "-"}</Paragraph>
                </section>

                <section>
                    <Text type="secondary">字段变更</Text>
                    {renderChangedFields(auditLog.changedFields)}
                </section>

                <section>
                    <Text type="secondary">快照对比</Text>
                    {renderSnapshotCompare(
                        auditLog.beforeSnapshot,
                        auditLog.afterSnapshot,
                        auditLog.changedFields
                    )}
                </section>
            </div>
        ) : null}
        {!auditLog && error ? <Empty description="审计详情加载失败" /> : null}
    </SandwishDrawer>
);
