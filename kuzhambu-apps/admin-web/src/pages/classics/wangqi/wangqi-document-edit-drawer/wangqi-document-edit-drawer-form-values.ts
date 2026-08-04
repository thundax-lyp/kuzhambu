import dayjs, { type Dayjs } from "dayjs";
import type { WangqiDocumentCommand } from "@/pages/classics/wangqi/wangqi-service";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";

export interface WangqiDocumentFormValues {
    content: string;
    contentFormat: string;
    documentTime: Dayjs | null;
    summary: string;
    title: string;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

export const toWangqiDocumentFormValues = (
    record?: WangqiDocumentRecord | null
): WangqiDocumentFormValues => {
    return {
        content: record?.content || "",
        contentFormat: record?.contentFormat || "MARKDOWN",
        documentTime: record?.documentTime ? dayjs(record.documentTime) : null,
        summary: record?.summary || "",
        title: record?.title || ""
    };
};

export const toWangqiDocumentCommand = (
    values: WangqiDocumentFormValues,
    record?: WangqiDocumentRecord | null
): WangqiDocumentCommand => {
    return {
        id: record?.id,
        title: normalizeText(values.title),
        summary: normalizeText(values.summary),
        contentFormat: normalizeText(values.contentFormat) || "MARKDOWN",
        content: normalizeText(values.content),
        documentTime: values.documentTime?.toISOString(),
        storageObjectId: record?.storageObjectId ?? null
    };
};
