import { Typography } from "antd";
import { useState } from "react";
import { KuzhambuSegmented, KuzhambuSpace } from "@/components";
import { SearchEventPanel } from "./search-event-panel";
import { SearchIndexRebuildPanel } from "./search-index-rebuild-panel";
import { SearchSummaryPanel } from "./search-summary-panel";
import "./search-statistic-page.css";

const { Text, Title } = Typography;

type SearchStatisticsPanel = "summary" | "records" | "rebuild";

export const SearchStatisticPage = () => {
    const [activePanel, setActivePanel] = useState<SearchStatisticsPanel>("summary");

    return (
        <main className="kuzhambu-page discovery-admin-page search-statistic-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Title level={2}>检索统计</Title>
                        <Text type="secondary">
                            查看检索统计、打开检索记录详情并手动触发索引重建。
                        </Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <KuzhambuSegmented
                        testId="discovery-search-statistics-section-segmented"
                        className="search-statistics-segmented"
                        options={[
                            { label: "统计摘要", value: "summary" },
                            { label: "检索记录", value: "records" },
                            { label: "索引重建", value: "rebuild" }
                        ]}
                        value={activePanel}
                        onChange={(value) => setActivePanel(value as SearchStatisticsPanel)}
                    />

                    <div hidden={activePanel !== "summary"}>
                        <SearchSummaryPanel />
                    </div>
                    <div hidden={activePanel !== "records"}>
                        <SearchEventPanel />
                    </div>
                    <div hidden={activePanel !== "rebuild"}>
                        <SearchIndexRebuildPanel />
                    </div>
                </KuzhambuSpace>
            </section>
        </main>
    );
};
