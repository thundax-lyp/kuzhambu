import { useMemo, useState } from "react";
import { KuzhambuTable, KuzhambuTag } from "@/components";

import type { GraphWorkbenchCandidateRecord } from "@/pages/knowledge/graph-extraction/graph-extraction-types";

interface GraphExtractionCandidatePreviewProps {
    candidate?: GraphWorkbenchCandidateRecord | null;
    loading?: boolean;
}

interface CandidateSpoRow {
    object: string;
    predicate: string;
    rowKey: string;
    subject: string;
}

const readString = (value: unknown) => {
    return typeof value === "string" ? value.trim() : "";
};

const readRelationField = (relation: Record<string, unknown>, keys: string[]) => {
    for (const key of keys) {
        const value = readString(relation[key]);
        if (value) {
            return value;
        }
    }
    return "";
};

const readCandidateRelations = (container: Record<string, unknown>) => {
    if (Array.isArray(container.relations)) {
        return container.relations;
    }
    if (Array.isArray(container.triples)) {
        return container.triples;
    }
    if (Array.isArray(container.spo)) {
        return container.spo;
    }
    return [];
};

const renderSpoTag = (label: string, value: string, type: "accent" | "info" | "neutral") => (
    <KuzhambuTag
        type={type}
        title={value || "-"}
        style={{
            display: "inline-flex",
            gap: 6,
            marginInlineEnd: 0,
            maxWidth: label === "P" ? 260 : 220,
            verticalAlign: "middle"
        }}
    >
        <span style={{ color: "#667085", fontSize: 12 }}>{label}</span>
        <span
            style={{
                fontWeight: 500,
                overflow: "hidden",
                textOverflow: "ellipsis",
                whiteSpace: "nowrap"
            }}
            title={value || "-"}
        >
            {value || "-"}
        </span>
    </KuzhambuTag>
);

const renderSpo = (row: CandidateSpoRow) => (
    <span
        style={{
            alignItems: "center",
            display: "inline-flex",
            gap: 8,
            whiteSpace: "nowrap"
        }}
    >
        {renderSpoTag("S", row.subject, "neutral")}
        <span style={{ color: "#667085", fontWeight: 600 }}>→</span>
        {renderSpoTag("P", row.predicate, "accent")}
        <span style={{ color: "#667085", fontWeight: 600 }}>→</span>
        {renderSpoTag("O", row.object, "info")}
    </span>
);

const parseCandidatePayload = (payload?: string | null): CandidateSpoRow[] => {
    if (!payload?.trim()) {
        return [];
    }

    try {
        const parsed = JSON.parse(payload) as unknown;
        const container =
            parsed && typeof parsed === "object" ? (parsed as Record<string, unknown>) : {};
        const relations = readCandidateRelations(container);

        return relations
            .filter((relation): relation is Record<string, unknown> =>
                Boolean(relation && typeof relation === "object")
            )
            .map((relation, index) => {
                const subject = readRelationField(relation, [
                    "subject",
                    "source",
                    "sourceName",
                    "head",
                    "from"
                ]);
                const predicate = readRelationField(relation, [
                    "predicate",
                    "relation",
                    "relationType",
                    "type",
                    "label"
                ]);
                const object = readRelationField(relation, [
                    "object",
                    "target",
                    "targetName",
                    "tail",
                    "to"
                ]);
                return {
                    object,
                    predicate,
                    rowKey:
                        readString(relation.id) ||
                        readString(relation.relationId) ||
                        `candidate-spo-${index}`,
                    subject
                };
            })
            .filter((relation) => relation.subject || relation.predicate || relation.object);
    } catch {
        return [];
    }
};

export const GraphExtractionCandidatePreview = ({
    candidate,
    loading = false
}: GraphExtractionCandidatePreviewProps) => {
    const parsedRows = useMemo(
        () => parseCandidatePayload(candidate?.candidatePayloadJson),
        [candidate?.candidatePayloadJson]
    );
    const candidateKey =
        candidate?.aiCandidateId || candidate?.taskId || candidate?.candidatePayloadJson || "empty";
    const [deletedRowKeysByCandidate, setDeletedRowKeysByCandidate] = useState<
        Record<string, string[]>
    >({});

    const rows = useMemo(
        () =>
            parsedRows.filter(
                (row) => !(deletedRowKeysByCandidate[candidateKey] || []).includes(row.rowKey)
            ),
        [candidateKey, deletedRowKeysByCandidate, parsedRows]
    );

    return (
        <KuzhambuTable<CandidateSpoRow>
            ariaLabel="候选 SPO 列表"
            dataSource={rows}
            loading={loading}
            pagination={false}
            rowKey="rowKey"
            size="small"
            columns={[
                {
                    key: "spo",
                    render: (_, record) => renderSpo(record),
                    title: "SPO"
                },
                {
                    key: "actions",
                    options: (record) => [
                        { key: "delete-divider", type: "divider" },
                        {
                            key: "delete",
                            text: "删除",
                            type: "danger",
                            testId: "knowledge-graph-extraction-candidate-delete-spo-button",
                            onClick: () =>
                                setDeletedRowKeysByCandidate((current) => ({
                                    ...current,
                                    [candidateKey]: Array.from(
                                        new Set([...(current[candidateKey] || []), record.rowKey])
                                    )
                                }))
                        }
                    ]
                }
            ]}
            locale={{
                emptyText: "暂无候选 SPO"
            }}
        />
    );
};
