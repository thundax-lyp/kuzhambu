import { KuzhambuDescriptions, KuzhambuExpandableText } from "@/components";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";
import "./wangqi-document-section-overview.css";

const readDocumentTitle = (document: WangqiDocumentRecord) => {
    return document.title?.trim() || `文档 ${document.id}`;
};

const readDocumentSummary = (document: WangqiDocumentRecord) => {
    return document.summary?.trim() || document.content?.trim() || "暂无简介/摘要";
};

const readDocumentMonth = (documentTime?: string | null) => {
    const normalizedDocumentTime = documentTime?.trim() || "";
    const matchedMonth = normalizedDocumentTime.match(/^(\d{4})-(\d{2})/);
    return matchedMonth ? `${matchedMonth[1]}-${matchedMonth[2]}` : normalizedDocumentTime || "—";
};

interface WangqiDocumentSectionOverviewProps {
    document: WangqiDocumentRecord;
    variant?: "default" | "qa";
}

export const WangqiDocumentSectionOverview = ({
    document,
    variant = "default"
}: WangqiDocumentSectionOverviewProps) => (
    <KuzhambuDescriptions
        ariaLabel="王圻文档基础信息"
        className="wangqi-detail-card wangqi-document-section-overview"
        column={3}
        colon={false}
        size="small"
        items={[
            { key: "time", label: "时间", children: readDocumentMonth(document.documentTime) },
            ...(variant === "qa"
                ? []
                : [{ key: "format", label: "格式", children: document.contentFormat || "—" }]),
            {
                key: "title",
                label: "标题",
                span: variant === "qa" ? 2 : 1,
                children: readDocumentTitle(document)
            },
            {
                key: "summary",
                label: "摘要",
                span: 3,
                children: (
                    <KuzhambuExpandableText
                        className="wangqi-document-section-overview-summary-text"
                        content={readDocumentSummary(document)}
                        collapsedRows={2}
                    />
                )
            }
        ]}
    />
);
