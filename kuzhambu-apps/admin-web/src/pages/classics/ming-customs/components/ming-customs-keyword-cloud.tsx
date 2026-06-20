import type { MingCustomsKeywordCloudItem } from "../ming-customs-types";

export interface MingCustomsKeywordCloudProps {
    items: MingCustomsKeywordCloudItem[];
    loading?: boolean;
    onSelect: (keyword: string) => void;
}

const readWeight = (count: number, maxCount: number) => {
    if (maxCount <= 0) {
        return 1;
    }
    return 1 + Math.min(count / maxCount, 1) * 0.42;
};

export const MingCustomsKeywordCloud = ({
    items,
    loading = false,
    onSelect
}: MingCustomsKeywordCloudProps) => {
    const maxCount = Math.max(...items.map((item) => item.count), 0);

    return (
        <aside className="ming-customs-keyword-cloud" aria-label="明代习俗关键词云">
            <div className="ming-customs-keyword-cloud-header">
                <span>关键词云</span>
                {loading ? (
                    <span className="ming-customs-keyword-cloud-loading">加载中</span>
                ) : null}
            </div>
            <div className="ming-customs-keyword-cloud-body">
                {items.length > 0 ? (
                    items.map((item) => (
                        <button
                            key={item.keyword}
                            type="button"
                            className="ming-customs-keyword-cloud-item"
                            style={{ fontSize: `${readWeight(item.count, maxCount)}rem` }}
                            aria-label={`筛选关键词 ${item.keyword}，${item.count} 次`}
                            onClick={() => onSelect(item.keyword)}
                        >
                            <span>{item.keyword}</span>
                            <strong>{item.count}</strong>
                        </button>
                    ))
                ) : (
                    <span className="ming-customs-keyword-cloud-empty">暂无关键词</span>
                )}
            </div>
        </aside>
    );
};
