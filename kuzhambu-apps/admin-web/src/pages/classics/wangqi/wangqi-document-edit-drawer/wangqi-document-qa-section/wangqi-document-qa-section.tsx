import type { ReactNode } from "react";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";
import { WangqiDocumentSectionOverview } from "../wangqi-document-section-overview";
import "./wangqi-document-qa-section.css";

interface WangqiDocumentQaSectionProps {
    document: WangqiDocumentRecord;
    content?: ReactNode;
}

export const WangqiDocumentQaSection = ({ document, content }: WangqiDocumentQaSectionProps) => (
    <div className="wangqi-document-qa-section">
        <WangqiDocumentSectionOverview document={document} variant="qa" />
        {content}
    </div>
);
