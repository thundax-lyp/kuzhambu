import { Segmented, Typography } from "antd";
import { useState } from "react";
import { KuzhambuSpace } from "@/components";
import { QaDiagnosticsPanel } from "./qa-diagnostics-panel";
import { QaHealthPanel } from "./qa-health-panel";
import { QaSessionTable } from "./qa-session-table";
import { QaSyncTable } from "./qa-sync-table";

import "./qa-console-page.css";

const { Text, Title } = Typography;

type QaConsolePanel = "health" | "sync" | "sessions" | "diagnostics";

const parseString = (value?: string | null) => {
    const trimmed = value?.trim() ?? "";
    return trimmed.length ? trimmed : null;
};

export const QaConsolePage = () => {
    const [activePanel, setActivePanel] = useState<QaConsolePanel>("health");
    const fastGptConsoleUrl = parseString(import.meta.env.VITE_FASTGPT_CONSOLE_URL);

    return (
        <main className="kuzhambu-page discovery-admin-page qa-console-page">
            <section>
                <header className="kuzhambu-page-header">
                    <div>
                        <Title level={2}>问答运维</Title>
                        <Text type="secondary">查看知识库健康、知识文档和问答会话。</Text>
                    </div>
                </header>

                <KuzhambuSpace orientation="vertical" size={16} style={{ width: "100%" }}>
                    <Segmented
                        className="qa-console-segmented"
                        options={[
                            { label: "健康状态", value: "health" },
                            { label: "知识文档", value: "sync" },
                            { label: "会话管理", value: "sessions" },
                            { label: "问答诊断", value: "diagnostics" }
                        ]}
                        value={activePanel}
                        onChange={(value) => setActivePanel(value as QaConsolePanel)}
                    />

                    {activePanel === "health" ? <QaHealthPanel /> : null}
                    {activePanel === "sync" ? <QaSyncTable /> : null}
                    {activePanel === "sessions" ? <QaSessionTable /> : null}
                    {activePanel === "diagnostics" ? (
                        <QaDiagnosticsPanel fastGptConsoleUrl={fastGptConsoleUrl} />
                    ) : null}
                </KuzhambuSpace>
            </section>
        </main>
    );
};
