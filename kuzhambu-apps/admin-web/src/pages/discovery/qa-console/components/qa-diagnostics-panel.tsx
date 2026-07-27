import { Typography } from "antd";
import { KuzhambuButton, KuzhambuSpace, KuzhambuCard } from "@/components";

const { Text } = Typography;

interface QaDiagnosticsPanelProps {
    fastGptConsoleUrl: string | null;
}

export const QaDiagnosticsPanel = ({ fastGptConsoleUrl }: QaDiagnosticsPanelProps) => {
    return (
        <KuzhambuCard title="问答诊断" size="small">
            <KuzhambuSpace orientation="vertical" size={12}>
                <Text type="secondary">知识条目、分段、召回配置以 FastGPT 为准。</Text>
                <KuzhambuButton
                    disabled={!fastGptConsoleUrl}
                    href={fastGptConsoleUrl ?? undefined}
                    target="_blank"
                    testId="discovery-qa-console-fastgpt-console-link"
                    type="primary"
                >
                    FastGPT 控制台
                </KuzhambuButton>
            </KuzhambuSpace>
        </KuzhambuCard>
    );
};
