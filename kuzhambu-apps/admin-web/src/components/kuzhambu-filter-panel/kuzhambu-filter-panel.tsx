import type { ReactNode } from "react";
import { Button, Col, Row } from "antd";
import "./kuzhambu-filter-panel.css";

export interface KuzhambuFilterPanelField {
    label: ReactNode;
    name: string;
    render: () => ReactNode;
}

export interface KuzhambuFilterPanelProps {
    actionsAlign?: "default" | "right";
    children?: ReactNode;
    className?: string;
    fields?: KuzhambuFilterPanelField[];
    onApply?: () => void;
    onReset?: () => void;
    open: boolean;
    resetDisabled?: boolean;
}

const resolveActionOffset = (fieldCount: number, columns: number, span: number) => {
    const usedColumns = fieldCount % columns;
    return (columns - usedColumns - 1) * span;
};

export const KuzhambuFilterPanel = ({
    actionsAlign = "default",
    children,
    className,
    fields,
    onApply,
    onReset,
    open,
    resetDisabled = false
}: KuzhambuFilterPanelProps) => {
    const fieldCount = fields?.length ?? 0;
    const structuredContent = fields?.length ? (
        <div className="kuzhambu-filter-panel-form">
            <Row className="kuzhambu-filter-panel-fields" gutter={[12, 12]} align="bottom">
                {fields.map((field) => (
                    <Col
                        className="kuzhambu-filter-panel-field"
                        key={field.name}
                        xs={24}
                        sm={12}
                        lg={6}
                    >
                        <div className="kuzhambu-filter-panel-label">{field.label}</div>
                        <div className="kuzhambu-filter-panel-control">{field.render()}</div>
                    </Col>
                ))}
                <Col
                    className="kuzhambu-filter-panel-field kuzhambu-filter-panel-action-field"
                    xs={{ span: 24, offset: 0 }}
                    sm={{ span: 12, offset: resolveActionOffset(fieldCount, 2, 12) }}
                    lg={{ span: 6, offset: resolveActionOffset(fieldCount, 4, 6) }}
                >
                    <div className="kuzhambu-filter-panel-actions">
                        <Button disabled={resetDisabled} onClick={onReset}>
                            重置
                        </Button>
                        <Button type="primary" onClick={onApply}>
                            查询
                        </Button>
                    </div>
                </Col>
            </Row>
        </div>
    ) : null;

    return (
        <div
            className={[
                "kuzhambu-filter-panel",
                open ? "kuzhambu-filter-panel-open" : "",
                actionsAlign === "right" ? "kuzhambu-filter-panel-actions-right" : "",
                className
            ]
                .filter(Boolean)
                .join(" ")}
        >
            {children ?? structuredContent}
        </div>
    );
};
