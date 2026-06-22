import { Tabs } from "antd";
import "./taxonomy-page.css";

const TAXONOMY_TABS = [
    { key: "categories", label: "标签分类", description: "标签分类管理" },
    { key: "tags", label: "统一标签", description: "统一标签管理" },
    { key: "reviews", label: "待审核标签", description: "待审核标签列表" },
    { key: "synonyms", label: "同义词", description: "同义词管理" }
];

export const TaxonomyPage = () => {
    return (
        <div className="knowledge-taxonomy-page">
            <Tabs
                items={TAXONOMY_TABS.map(({ key, label, description }) => ({
                    key,
                    label,
                    children: <div className="knowledge-taxonomy-empty">{description}</div>
                }))}
            />
        </div>
    );
};
