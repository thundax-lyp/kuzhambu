import { Tag } from "antd";
import { KuzhambuAlert } from "@/components/kuzhambu-alert";
import { KuzhambuSpace } from "@/components/kuzhambu-space";

export interface CapabilityModelTagMatch {
    matched: boolean;
    missingTags: string[];
    modelTags: string[];
    requiredTags: string[];
}

interface CapabilityModelMatchPanelProps {
    tagMatch: CapabilityModelTagMatch;
}

export const CapabilityModelMatchPanel = ({ tagMatch }: CapabilityModelMatchPanelProps) => {
    return (
        <div className="capability-mappings-match">
            <div className="capability-mappings-match-row">
                <span>requiredTags</span>
                <KuzhambuSpace>
                    {tagMatch.requiredTags.map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                    ))}
                    {tagMatch.requiredTags.length === 0 ? "-" : null}
                </KuzhambuSpace>
            </div>
            <div className="capability-mappings-match-row">
                <span>modelTags</span>
                <KuzhambuSpace>
                    {tagMatch.modelTags.map((tag) => (
                        <Tag key={tag}>{tag}</Tag>
                    ))}
                    {tagMatch.modelTags.length === 0 ? "-" : null}
                </KuzhambuSpace>
            </div>
            <KuzhambuAlert
                type={tagMatch.matched ? "success" : "warning"}
                showIcon
                title={
                    tagMatch.matched
                        ? "能力标签匹配"
                        : `缺少标签：${tagMatch.missingTags.join(", ")}`
                }
            />
        </div>
    );
};
