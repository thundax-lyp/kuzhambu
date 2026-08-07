import { useState } from "react";
import { ClockCircleOutlined } from "@ant-design/icons";
import { Empty, Typography } from "antd";
import { KuzhambuDrawer, KuzhambuButton, KuzhambuTimeline as Timeline } from "@/components";
import type { WangqiDocumentRecord } from "@/pages/classics/wangqi/wangqi-types";

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
                ariaLabel="打开王圻文档时间线"
                onClick={() => setOpen(true)}
            >
                时间线
            </KuzhambuButton>
            <KuzhambuDrawer
                testId="classics-wangqi-wangqi-timeline-drawer"
                aria-label="王圻文档时间线"
                bodyLayout="timeline"
                className="wangqi-timeline-drawer"
                destroyOnHidden
                loading={loading}
                open={open}
                size="large"
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
                        testId="classics-wangqi-wangqi-timeline"
                        mode="alternate"
                        className="wangqi-timeline"
                        items={dataSource.map((record) => ({
                            key: record.id,
                            title: (
                                <span className="wangqi-timeline-item-date">
                                    {readTimelineTitle(record)}
                                </span>
                            ),
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
