import type { MingCustomsCommand } from "@/pages/classics/ming-custom/ming-custom-service";
import type { MingCustomsRecord } from "@/pages/classics/ming-custom/ming-custom-types";

export interface MingCustomsFormValues {
    category: string;
    chapter: string;
    content: string;
    contentFormat: string;
    originalExcerpts: string;
    section: string;
    summary: string;
    title: string;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

export const toMingCustomsFormValues = (
    record?: MingCustomsRecord | null
): MingCustomsFormValues => {
    return {
        category: record?.category || "",
        chapter: record?.chapter || "",
        content: record?.content || "",
        contentFormat: record?.contentFormat || "MARKDOWN",
        originalExcerpts: record?.originalExcerpts || "",
        section: record?.section || "",
        summary: record?.summary || "",
        title: record?.title || ""
    };
};

export const toMingCustomsCommand = (
    values: MingCustomsFormValues,
    record?: MingCustomsRecord | null
): MingCustomsCommand => {
    return {
        id: record?.id,
        title: normalizeText(values.title),
        category: normalizeText(values.category),
        chapter: normalizeText(values.chapter),
        section: normalizeText(values.section),
        summary: normalizeText(values.summary),
        contentFormat: normalizeText(values.contentFormat) || "MARKDOWN",
        content: normalizeText(values.content),
        originalExcerpts: normalizeText(values.originalExcerpts)
    };
};
