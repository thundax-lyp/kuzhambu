import { diffWordsWithSpace } from "diff";
import "./kuzhambu-text-compare.css";

export interface KuzhambuTextCompareProps {
    baseline?: string | null;
    candidate?: string | null;
    className?: string;
    emptyText?: string;
    testId?: string;
    title?: string;
}

export const KuzhambuTextCompare = ({
    baseline,
    candidate,
    className,
    emptyText = "暂无差异",
    testId,
    title = "差异对比"
}: KuzhambuTextCompareProps) => {
    const changes = diffWordsWithSpace(baseline || "", candidate || "");
    const hasChanges = changes.some((change) => change.added || change.removed);
    const classNames = ["kuzhambu-text-compare", className].filter(Boolean).join(" ");
    const readChangeClassName = (change: (typeof changes)[number]) => {
        if (change.added) {
            return "is-added";
        }
        if (change.removed) {
            return "is-removed";
        }
        return "is-unchanged";
    };

    return (
        <section className={classNames} data-testid={testId} aria-label={title}>
            <div className="kuzhambu-text-compare-title">{title}</div>
            <div className="kuzhambu-text-compare-content">
                {hasChanges ? (
                    changes.map((change, index) => {
                        const changeClassName = readChangeClassName(change);
                        return (
                            <span key={`${index}-${change.value}`} className={changeClassName}>
                                {change.value}
                            </span>
                        );
                    })
                ) : (
                    <span className="kuzhambu-text-compare-empty">{emptyText}</span>
                )}
            </div>
        </section>
    );
};
