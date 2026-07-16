import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { TagsOutlined } from "@ant-design/icons";
import { Badge, Empty } from "antd";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import * as service from "../ming-customs-service";
import type { MingCustomsTagCloudItem } from "../ming-customs-types";
import { KuzhambuButton } from "@/components/kuzhambu-button";

export interface MingCustomsTagCloudProps {
    category?: string | null;
    keyword?: string | null;
    onSelect: (item: MingCustomsTagCloudItem) => void;
    visibility?: string | null;
}

const readWeight = (count: number, maxCount: number) => {
    if (maxCount <= 0) {
        return 1;
    }
    return 1 + Math.min(count / maxCount, 1) * 0.42;
};

export const MingCustomsTagCloud = ({
    category,
    keyword,
    onSelect,
    visibility
}: MingCustomsTagCloudProps) => {
    const [open, setOpen] = useState(false);
    const tagCloudQuery = useQuery({
        queryKey: ["ming-customs", "tag-cloud", category, keyword, visibility],
        queryFn: () => service.listTagCloud({ category, keyword, visibility }),
        enabled: open,
        retry: false
    });
    const items = tagCloudQuery.data || [];
    const maxCount = Math.max(...items.map((item) => item.count), 0);

    return (
        <>
            <KuzhambuButton name="标签云" icon={<TagsOutlined />} onClick={() => setOpen(true)}>
                标签云
            </KuzhambuButton>
            <KuzhambuDrawer
                aria-label="明代习俗标签云"
                destroyOnHidden
                loading={tagCloudQuery.isLoading}
                open={open}
                size="middle"
                title="标签云"
                onClose={() => setOpen(false)}
                footer={
                    <KuzhambuButton name="关闭" type="primary" onClick={() => setOpen(false)}>
                        关闭
                    </KuzhambuButton>
                }
            >
                {items.length > 0 ? (
                    <div className="ming-customs-keyword-cloud-body">
                        {items.map((item) => (
                            <button
                                key={`${item.tagId ?? "name"}-${item.tagNameSnapshot}`}
                                type="button"
                                className="ming-customs-keyword-cloud-item"
                                style={{ fontSize: `${readWeight(item.count, maxCount)}rem` }}
                                aria-label={`筛选标签 ${item.tagNameSnapshot}，${item.count} 条`}
                                onClick={() => {
                                    setOpen(false);
                                    onSelect(item);
                                }}
                            >
                                <span>{item.tagNameSnapshot}</span>
                                <Badge
                                    count={item.count}
                                    color="var(--ming-customs-accent-color)"
                                />
                            </button>
                        ))}
                    </div>
                ) : (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无标签" />
                )}
            </KuzhambuDrawer>
        </>
    );
};
