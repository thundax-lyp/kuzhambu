import type { ReactNode } from "react";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";
import { WangqiDocumentSectionOverview } from "../wangqi-document-section-overview";
import "./wangqi-document-tags-section.css";

interface WangqiDocumentTagsSectionProps {
    document: WangqiDocumentRecord;
    content?: ReactNode;
}

export const WangqiDocumentTagsSection = ({
    document,
    content
}: WangqiDocumentTagsSectionProps) => (
    <div className="wangqi-document-tags-section">
        <WangqiDocumentSectionOverview document={document} />
        {content}
    </div>
);
