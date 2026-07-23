import { useState } from "react";
import { ClockCircleOutlined } from "@ant-design/icons";
import { Empty, Timeline, Typography } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { WangqiDocumentRecord } from "../wangqi-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

const { Text } = Typography;

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "未填写时间";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    return `${year}/${month}`;
};

const readTimelineTitle = (record: WangqiDocumentRecord) => {
    const event = record.events?.[0];
    return (
        event?.occurredLabel ||
        (event?.occurredAt ? formatDateTime(event.occurredAt) : formatDateTime(record.documentTime))
    );
};

const readTimelineItemTitle = (record: WangqiDocumentRecord) => {
    return record.events?.[0]?.title || record.title || "未命名文档";
};

interface WangqiTimelineProps {
    dataSource: WangqiDocumentRecord[];
    loading?: boolean;
    onOpenDocument: (record: WangqiDocumentRecord) => void;
}

export const WangqiTimeline = ({
    dataSource,
    loading = false,
    onOpenDocument
}: WangqiTimelineProps) => {
    const [open, setOpen] = useState(false);

    return (
        <>
            <KuzhambuButton
                testId="classics-wangqi-wangqi-timeline-action-button"
                icon={<ClockCircleOutlined />}
                onClick={() => setOpen(true)}
            >
                时间线
            </KuzhambuButton>
            <KuzhambuDrawer
                testId="classics-wangqi-wangqi-timeline-drawer"
                aria-label="王圻文档时间线"
                destroyOnHidden
                loading={loading}
                open={open}
                size="middle"
                title="时间线"
                onClose={() => setOpen(false)}
                footerActions={[
                    {
                        testId: "classics-wangqi-wangqi-timeline-close-button",
                        title: "关闭",
                        type: "primary",
                        action: () => setOpen(false)
                    }
                ]}
            >
                {dataSource.length === 0 ? (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无王圻时间线" />
                ) : (
                    <Timeline
                        items={dataSource.map((record) => ({
                            key: record.id,
                            title: readTimelineTitle(record),
                            content: (
                                <KuzhambuButton
                                    testId="classics-wangqi-wangqi-timeline-action-button-2"
                                    type="text"
                                    className="wangqi-timeline-item"
                                    onClick={() => {
                                        setOpen(false);
                                        onOpenDocument(record);
                                    }}
                                >
                                    <span className="wangqi-timeline-item-title">
                                        {readTimelineItemTitle(record)}
                                    </span>
                                    <Text
                                        type="secondary"
                                        ellipsis
                                        className="wangqi-timeline-item-summary"
                                    >
                                        {record.summary || "暂无摘要"}
                                    </Text>
                                </KuzhambuButton>
                            )
                        }))}
                    />
                )}
            </KuzhambuDrawer>
        </>
    );
};
