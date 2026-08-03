import { Descriptions } from "antd";
import { KuzhambuDrawer } from "@/components";
import type { AiInvocationLogRecord } from "../invocations-types";

import "./invocation-detail-drawer.css";

const formatWarnings = (value?: string | null) => {
    if (!value) {
        return "-";
    }
    try {
        return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
        return value;
    }
};

interface InvocationDetailDrawerProps {
    call?: AiInvocationLogRecord | null;
    open: boolean;
    onClose: () => void;
}

export const InvocationDetailDrawer = ({ call, open, onClose }: InvocationDetailDrawerProps) => {
    return (
        <KuzhambuDrawer
            testId="ai-invocations-invocation-detail-drawer"
            aria-label="AI 调用详情"
            destroyOnHidden
            open={open}
            size="large"
            title="调用详情"
            onClose={onClose}
        >
            {call ? (
                <Descriptions column={1} size="small">
                    <Descriptions.Item label="请求ID">{call.requestId || "-"}</Descriptions.Item>
                    <Descriptions.Item label="链路ID">{call.traceId || "-"}</Descriptions.Item>
                    <Descriptions.Item label="提示词版本ID">
                        {call.promptVersionId || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="是否流式">
                        {call.streamUsed ? "是" : "否"}
                    </Descriptions.Item>
                    <Descriptions.Item label="流式是否完成">
                        {call.streamCompleted ? "是" : "否"}
                    </Descriptions.Item>
                    <Descriptions.Item label="输入 Tokens">
                        {call.inputTokens ?? "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="输出 Tokens">
                        {call.outputTokens ?? "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="失败阶段">
                        {call.failureStage || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="结果格式">
                        {call.resultFormat || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="错误类型">{call.errorType || "-"}</Descriptions.Item>
                    <Descriptions.Item label="错误信息">
                        {call.errorMessage || "-"}
                    </Descriptions.Item>
                    <Descriptions.Item label="警告 JSON">
                        <pre className="invocations-warnings">
                            {formatWarnings(call.warningsJson)}
                        </pre>
                    </Descriptions.Item>
                </Descriptions>
            ) : null}
        </KuzhambuDrawer>
    );
};
