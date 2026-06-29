export type ClassicsContentType = "SANCAI_ENTRY" | "WANGQI_DOCUMENT" | "MING_CUSTOMS" | string;

export interface ClassicsContentRef {
    contentId: number;
    contentType: ClassicsContentType;
}

export interface ClassicsContentTagRecord {
    id?: number | null;
    tagId?: number | null;
    contentType?: ClassicsContentType | null;
    contentId?: number | null;
    tagNameSnapshot?: string | null;
    status?: string | null;
    source?: string | null;
}

export interface ClassicsContentTagPayload extends ClassicsContentRef {
    id?: number | null;
    tagId?: number | null;
    tagNameSnapshot: string;
    status?: string | null;
    source?: string | null;
}

export interface ClassicsContentTagSortPayload extends ClassicsContentRef {
    orderedIds: number[];
    sortDirection?: string | null;
}

export interface ClassicsContentQaPairRecord {
    id?: number | null;
    contentType?: ClassicsContentType | null;
    contentId?: number | null;
    question?: string | null;
    answer?: string | null;
    source?: string | null;
}

export interface ClassicsContentQaPairPayload extends ClassicsContentRef {
    id?: number | null;
    question: string;
    answer: string;
    source?: string | null;
}

export interface ClassicsContentQaPairSortPayload {
    orderedIds: number[];
    sortDirection?: string | null;
}

export interface ClassicsContentListPayload {
    contentType: ClassicsContentType;
    contentId: number;
}
