import { Descriptions, Typography } from "antd";
import { KuzhambuDrawer, KuzhambuSpace } from "@/components";

import type { DiscoveryQaSessionRecord } from "../qa-types";

const { Text } = Typography;

const formatTime = (value?: number | null) => {
    if (!value) {
        return "-";
    }
    return new Intl.DateTimeFormat("zh-CN", {
        dateStyle: "medium"
    }).format(new Date(value));
};

const sessionTitle = (session?: DiscoveryQaSessionRecord) => {
    if (session?.title) {
        return session.title;
    }
    return session?.sessionId ? `会话 ${session.sessionId}` : "未命名会话";
};

interface QaSessionDetailDrawerProps {
    onClose: () => void;
    open: boolean;
    session?: DiscoveryQaSessionRecord;
}

export const QaSessionDetailDrawer = ({ onClose, open, session }: QaSessionDetailDrawerProps) => {
    return (
        <KuzhambuDrawer
            destroyOnClose
            onClose={onClose}
            open={open}
            size="large"
            testId="discovery-qa-session-detail-drawer"
            title={session ? sessionTitle(session) : "会话详情"}
        >
            {session ? (
                <KuzhambuSpace orientation="vertical" size={12} style={{ width: "100%" }}>
                    <Descriptions
                        bordered
                        column={2}
                        items={[
                            {
                                key: "sessionId",
                                label: "会话 ID",
                                children: session.sessionId ?? "-"
                            },
                            { key: "scope", label: "作用域", children: session.scope ?? "-" },
                            {
                                key: "contextMode",
                                label: "上下文模式",
                                children: session.contextMode ?? "-"
                            },
                            {
                                key: "openedAt",
                                label: "创建时间",
                                children: formatTime(session.openedAt)
                            },
                            {
                                key: "lastMessageAt",
                                label: "最后消息",
                                children: formatTime(session.lastMessageAt)
                            },
                            { key: "status", label: "状态", children: session.status ?? "-" }
                        ]}
                        size="small"
                    />
                    <Text type="secondary">消息数：{session.messages?.length ?? 0}</Text>
                </KuzhambuSpace>
            ) : null}
        </KuzhambuDrawer>
    );
};
