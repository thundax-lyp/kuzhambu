import type { ReactNode } from "react";

interface KuzhambuListPageTableAreaProps {
    aside: ReactNode;
    asideClassName?: string;
    areaClassName?: string;
    children: ReactNode;
    placement?: "left" | "right";
}

// AI NOTE: This is an internal layout helper for KuzhambuListPage table + aside composition.
// Pages should usually configure tableAside on KuzhambuListPage instead of importing this directly.
export const KuzhambuListPageTableArea = ({
    aside,
    asideClassName,
    areaClassName,
    children,
    placement = "right"
}: KuzhambuListPageTableAreaProps) => (
    <div
        className={[
            "kuzhambu-list-page-table-area",
            `kuzhambu-list-page-table-area-aside-${placement}`,
            areaClassName
        ]
            .filter(Boolean)
            .join(" ")}
    >
        <div className="kuzhambu-list-page-table-main">{children}</div>
        <aside
            className={["kuzhambu-list-page-table-aside", asideClassName].filter(Boolean).join(" ")}
        >
            {aside}
        </aside>
    </div>
);
