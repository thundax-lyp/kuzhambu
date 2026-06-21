import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { TagsOutlined } from "@ant-design/icons";
import { Badge, Button, Empty } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import * as service from "../ming-customs-service";

export interface MingCustomsKeywordCloudProps {
    onSelect: (keyword: string) => void;
    visibility?: string | null;
}

const readWeight = (count: number, maxCount: number) => {
    if (maxCount <= 0) {
        return 1;
    }
    return 1 + Math.min(count / maxCount, 1) * 0.42;
};

export const MingCustomsKeywordCloud = ({ onSelect, visibility }: MingCustomsKeywordCloudProps) => {
    const [open, setOpen] = useState(false);
    const keywordCloudQuery = useQuery({
        queryKey: ["ming-customs", "keyword-cloud", visibility],
        queryFn: () => service.listKeywordCloud(visibility),
        enabled: open,
        retry: false
    });
    const items = keywordCloudQuery.data || [];
    const maxCount = Math.max(...items.map((item) => item.count), 0);

    return (
        <>
            <Button icon={<TagsOutlined />} onClick={() => setOpen(true)}>
                关键词云
            </Button>
            <KuzhambuDrawer
                aria-label="明代习俗关键词云"
                destroyOnHidden
                loading={keywordCloudQuery.isLoading}
                open={open}
                size="middle"
                title="关键词云"
                onClose={() => setOpen(false)}
                footer={
                    <Button type="primary" onClick={() => setOpen(false)}>
                        关闭
                    </Button>
                }
            >
                {items.length > 0 ? (
                    <div className="ming-customs-keyword-cloud-body">
                        {items.map((item) => (
                            <button
                                key={item.keyword}
                                type="button"
                                className="ming-customs-keyword-cloud-item"
                                style={{ fontSize: `${readWeight(item.count, maxCount)}rem` }}
                                aria-label={`筛选关键词 ${item.keyword}，${item.count} 次`}
                                onClick={() => {
                                    setOpen(false);
                                    onSelect(item.keyword);
                                }}
                            >
                                <span>{item.keyword}</span>
                                <Badge
                                    count={item.count}
                                    color="var(--ming-customs-accent-color)"
                                />
                            </button>
                        ))}
                    </div>
                ) : (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无关键词" />
                )}
            </KuzhambuDrawer>
        </>
    );
};
