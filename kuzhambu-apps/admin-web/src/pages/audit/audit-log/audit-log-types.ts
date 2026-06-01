export interface AuditFieldRecord {
    fieldName?: string | null;
    fieldLabel?: string | null;
    beforeDisplayValue?: string | null;
    afterDisplayValue?: string | null;
}

export interface AuditSnapshotFieldRecord {
    fieldName?: string | null;
    fieldLabel?: string | null;
    displayValue?: string | null;
    valueType?: string | null;
    sensitive?: boolean | null;
}

export interface AuditSnapshotRecord {
    objectType?: string | null;
    objectId?: string | null;
    displayName?: string | null;
    fields?: AuditSnapshotFieldRecord[] | null;
}

export interface AuditLogRecord {
    id: string;
    objectType?: string | null;
    objectId?: string | null;
    objectDisplayName?: string | null;
    objectTypeLabel?: string | null;
    version?: number | null;
    action?: string | null;
    actionLabel?: string | null;
    operatorType?: string | null;
    operatorTypeLabel?: string | null;
    operatorId?: string | null;
    operatorName?: string | null;
    source?: string | null;
    requestId?: string | null;
    traceId?: string | null;
    remoteAddr?: string | null;
    summary?: string | null;
    occurredAt?: string | null;
    changedFields?: AuditFieldRecord[] | null;
}

export interface AuditLogDetailRecord extends AuditLogRecord {
    idempotencyKey?: string | null;
    previousVersion?: number | null;
    beforeSnapshot?: AuditSnapshotRecord | null;
    afterSnapshot?: AuditSnapshotRecord | null;
}
