import { Segmented } from "antd";
import type { SegmentedProps } from "antd";
import type { ReactNode } from "react";
import { KuzhambuDrawer } from "@/components/kuzhambu-drawer";
import type { KuzhambuDrawerProps } from "@/components/kuzhambu-drawer";
import "./kuzhambu-segmented-drawer.css";

export interface KuzhambuSegmentedDrawerSection<TSection extends string = string> {
    content: ReactNode;
    disabled?: boolean;
    label: ReactNode;
    value: TSection;
    visible?: boolean;
}

export interface KuzhambuSegmentedDrawerProps<TSection extends string = string> extends Omit<
    KuzhambuDrawerProps,
    "children" | "extra"
> {
    activeSection: TSection;
    children?: ReactNode;
    extraClassName?: string;
    headerExtra?: ReactNode;
    onSectionChange: (section: TSection) => void;
    renderSectionContent?: (content: ReactNode) => ReactNode;
    sectionClassName?: string;
    sections: Array<KuzhambuSegmentedDrawerSection<TSection>>;
    segmentedClassName?: string;
    showSegmented?: boolean;
}

export const KuzhambuSegmentedDrawer = <TSection extends string = string>({
    activeSection,
    children,
    extraClassName,
    headerExtra,
    onSectionChange,
    renderSectionContent,
    sectionClassName,
    sections,
    segmentedClassName,
    showSegmented = true,
    ...drawerProps
}: KuzhambuSegmentedDrawerProps<TSection>) => {
    const visibleSections = sections.filter((section) => section.visible !== false);
    const activeVisibleSection =
        visibleSections.find((section) => section.value === activeSection) ?? visibleSections[0];
    const activeSectionContent = activeVisibleSection?.content;
    const segmentedOptions: SegmentedProps<TSection>["options"] = visibleSections.map(
        (section) => ({
            disabled: section.disabled,
            label: section.label,
            value: section.value
        })
    );

    const extraContent =
        showSegmented || headerExtra ? (
            <div
                className={["kuzhambu-segmented-drawer-extra", extraClassName]
                    .filter(Boolean)
                    .join(" ")}
            >
                {showSegmented ? (
                    <Segmented<TSection>
                        className={segmentedClassName}
                        options={segmentedOptions}
                        value={activeVisibleSection?.value}
                        onChange={onSectionChange}
                    />
                ) : null}
                {headerExtra}
            </div>
        ) : undefined;

    return (
        <KuzhambuDrawer {...drawerProps} extra={extraContent}>
            {children}
            <div
                className={["kuzhambu-segmented-drawer-section", sectionClassName]
                    .filter(Boolean)
                    .join(" ")}
            >
                {renderSectionContent
                    ? renderSectionContent(activeSectionContent)
                    : activeSectionContent}
            </div>
        </KuzhambuDrawer>
    );
};
