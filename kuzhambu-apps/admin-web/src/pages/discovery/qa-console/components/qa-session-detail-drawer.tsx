import { Descriptions, Typography } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import type { DiscoveryQaSessionDetailRecord } from "../qa-console-types";

const { Text } = Typography;

const formatContentType = (value?: string | null) => {
    if (value === "SANCAI_ENTRY") {
        return "三才图会";
    }
    return value ?? "-";
};

const formatTime = (value?: number | string | null) => {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    const timestamp = typeof value === "number" ? value : Date.parse(value);
    if (Number.isNaN(timestamp)) {
        return String(value);
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(timestamp));
};

export interface QaSessionDetailDrawerProps {
    onClose: () => void;
    open: boolean;
    sessionDetail: DiscoveryQaSessionDetailRecord | null;
}

export const QaSessionDetailDrawer = ({
    onClose,
    open,
    sessionDetail
}: QaSessionDetailDrawerProps) => {
    return (
        <KuzhambuDrawer
            destroyOnClose
            onClose={onClose}
            open={open}
            size="large"
            testId="discovery-qa-console-session-detail-drawer"
            title={sessionDetail?.title ?? "会话详情"}
        >
            {sessionDetail ? (
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    <Descriptions
                        bordered
                        column={2}
                        items={[
                            {
                                key: "scope",
                                label: "作用域",
                                children: sessionDetail.scope ?? "-"
                            },
                            {
                                key: "contextMode",
                                label: "上下文模式",
                                children: sessionDetail.contextMode ?? "-"
                            },
                            {
                                key: "contextContentType",
                                label: "上下文内容类型",
                                children: formatContentType(sessionDetail.contextContentType)
                            },
                            {
                                key: "lastMessageAt",
                                label: "最后消息",
                                children: formatTime(sessionDetail.lastMessageAt)
                            }
                        ]}
                        size="small"
                    />

                    <KuzhambuSpace orientation="vertical" size={8} style={{ width: "100%" }}>
                        <Text strong>消息</Text>
                        {sessionDetail.messages?.length ? (
                            sessionDetail.messages.map((message) => (
                                <div
                                    className="qa-console-message"
                                    key={message.messageId ?? message.content}
                                >
                                    <Text strong>
                                        {message.role ?? "-"} · {message.messageStatus ?? "-"}
                                    </Text>
                                    <Text>{message.content ?? "-"}</Text>
                                    <Text type="secondary">
                                        发送 {formatTime(message.sentAt)} · 回答{" "}
                                        {formatTime(message.answeredAt)}
                                    </Text>
                                </div>
                            ))
                        ) : (
                            <Text type="secondary">暂无消息</Text>
                        )}
                    </KuzhambuSpace>
                </KuzhambuSpace>
            ) : null}
        </KuzhambuDrawer>
    );
};
