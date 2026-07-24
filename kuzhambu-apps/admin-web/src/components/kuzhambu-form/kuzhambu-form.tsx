import { Col, Form, Row } from "antd";
import type { ColProps, FormItemProps, FormProps, RowProps } from "antd";
import {
    Children,
    Fragment,
    cloneElement,
    isValidElement,
    type ReactElement,
    type ReactNode
} from "react";
import {
    KUZHAMBU_FORM_ITEM_LAYOUTS,
    type KuzhambuFormItemLayoutSize,
    type KuzhambuFormLayoutTier
} from "./kuzhambu-form-layout";
import { useContainerLayoutTier } from "./hooks/use-container-layout-tier";

export interface KuzhambuFormProps<Values = unknown> extends Omit<
    FormProps<Values>,
    "children" | "layout"
> {
    children?: ReactNode;
    rowGutter?: RowProps["gutter"];
}

export interface KuzhambuFormItemProps extends Omit<FormItemProps, "labelCol" | "wrapperCol"> {
    colProps?: Omit<ColProps, "lg" | "md" | "offset" | "sm" | "span" | "xl" | "xs" | "xxl">;
    layoutSize?: KuzhambuFormItemLayoutSize;
    layoutTier?: KuzhambuFormLayoutTier;
}

export type KuzhambuFormHiddenItemProps = Omit<FormItemProps, "hidden" | "labelCol" | "wrapperCol">;

export interface KuzhambuFormPlaceholderItemProps {
    children?: ReactNode;
    colProps?: Omit<ColProps, "lg" | "md" | "offset" | "sm" | "span" | "xl" | "xs" | "xxl">;
    fillLine?: boolean;
    layoutSize?: KuzhambuFormItemLayoutSize;
}

export const KuzhambuFormItem = ({
    layoutSize = "middle",
    layoutTier,
    ...formItemProps
}: KuzhambuFormItemProps) => {
    const layout = KUZHAMBU_FORM_ITEM_LAYOUTS[layoutSize];
    const labelCol = layoutTier ? layout.labelCol[layoutTier] : layout.labelCol;
    const wrapperCol = layoutTier ? layout.wrapperCol[layoutTier] : layout.wrapperCol;

    return <Form.Item {...formItemProps} labelCol={labelCol} wrapperCol={wrapperCol} />;
};

export const KuzhambuFormHiddenItem = ({ ...formItemProps }: KuzhambuFormHiddenItemProps) => {
    return <Form.Item {...formItemProps} hidden />;
};

export const KuzhambuFormPlaceholderItem = (props: KuzhambuFormPlaceholderItemProps) => {
    void props;
    return null;
};

type KuzhambuFormItemElement = ReactElement<KuzhambuFormItemProps>;
type KuzhambuFormHiddenItemElement = ReactElement<KuzhambuFormHiddenItemProps>;
type KuzhambuFormPlaceholderItemElement = ReactElement<KuzhambuFormPlaceholderItemProps>;
type KuzhambuFormLayoutElement =
    | { element: KuzhambuFormItemElement; kind: "item" }
    | { element: KuzhambuFormHiddenItemElement; kind: "hidden" }
    | { element: KuzhambuFormPlaceholderItemElement; kind: "placeholder" };

interface FormLayoutItem {
    ariaHidden?: boolean;
    child: ReactNode;
    colProps: ColProps;
}

const isFragmentElement = (child: ReactNode): child is ReactElement<{ children?: ReactNode }> => {
    return isValidElement(child) && child.type === Fragment;
};

const isKuzhambuFormItemElement = (child: ReactNode): child is KuzhambuFormItemElement => {
    return isValidElement(child) && child.type === KuzhambuFormItem;
};

const isKuzhambuFormHiddenItemElement = (
    child: ReactNode
): child is KuzhambuFormHiddenItemElement => {
    return isValidElement(child) && child.type === KuzhambuFormHiddenItem;
};

const isKuzhambuFormPlaceholderItemElement = (
    child: ReactNode
): child is KuzhambuFormPlaceholderItemElement => {
    return isValidElement(child) && child.type === KuzhambuFormPlaceholderItem;
};

const collectLayoutChildren = (children: ReactNode): KuzhambuFormLayoutElement[] => {
    return Children.toArray(children).flatMap((child) => {
        if (isFragmentElement(child)) {
            return collectLayoutChildren(child.props.children);
        }
        if (isKuzhambuFormItemElement(child)) {
            return [{ element: child, kind: "item" }];
        }
        if (isKuzhambuFormHiddenItemElement(child)) {
            return [{ element: child, kind: "hidden" }];
        }
        if (isKuzhambuFormPlaceholderItemElement(child)) {
            return [{ element: child, kind: "placeholder" }];
        }
        return [];
    });
};

const readItemSpan = (
    layoutSize: KuzhambuFormItemLayoutSize | undefined,
    tier: KuzhambuFormLayoutTier
) => {
    return KUZHAMBU_FORM_ITEM_LAYOUTS[layoutSize || "middle"].col[tier];
};

const readFormItemColProps = (
    props: KuzhambuFormItemProps,
    layoutTier: KuzhambuFormLayoutTier
): ColProps => ({
    ...props.colProps,
    span: readItemSpan(props.layoutSize, layoutTier)
});

const readPlaceholderColProps = (
    props: KuzhambuFormPlaceholderItemProps,
    currentSpan: number,
    layoutTier: KuzhambuFormLayoutTier
): ColProps => ({
    ...props.colProps,
    span: props.fillLine
        ? Math.max(24 - currentSpan, 0)
        : readItemSpan(props.layoutSize, layoutTier)
});

const hasRenderablePlaceholderChildren = (children: ReactNode) => {
    return children !== null && children !== undefined && children !== false;
};

const buildFormRows = (children: ReactNode, layoutTier: KuzhambuFormLayoutTier) => {
    const hiddenItems: ReactNode[] = [];
    const rows: FormLayoutItem[][] = [];
    let currentRow: FormLayoutItem[] = [];
    let currentSpan = 0;

    const pushCurrentRow = () => {
        if (!currentRow.length) {
            return;
        }
        rows.push(currentRow);
        currentRow = [];
        currentSpan = 0;
    };

    collectLayoutChildren(children).forEach((layoutChild) => {
        if (layoutChild.kind === "hidden") {
            hiddenItems.push(layoutChild.element);
            return;
        }

        if (layoutChild.kind === "placeholder") {
            const child = layoutChild.element;
            const itemSpan = child.props.fillLine
                ? Math.max(24 - currentSpan, 0)
                : readItemSpan(child.props.layoutSize, layoutTier);
            const hasChildren = hasRenderablePlaceholderChildren(child.props.children);
            if (currentRow.length && currentSpan + itemSpan > 24) {
                pushCurrentRow();
            }
            if (!hasChildren && itemSpan >= 24 && !currentRow.length) {
                return;
            }
            currentRow.push({
                ariaHidden: !hasChildren,
                child: hasChildren ? child.props.children : null,
                colProps: readPlaceholderColProps(child.props, currentSpan, layoutTier)
            });
            currentSpan += itemSpan;
            if (currentSpan >= 24) {
                pushCurrentRow();
            }
            return;
        }

        const child = layoutChild.element;
        const itemSpan = readItemSpan(child.props.layoutSize, layoutTier);
        if (currentRow.length && currentSpan + itemSpan > 24) {
            pushCurrentRow();
        }
        currentRow.push({
            child: cloneElement(child, { layoutTier }),
            colProps: readFormItemColProps(child.props, layoutTier)
        });
        currentSpan += itemSpan;
        if (currentSpan >= 24) {
            pushCurrentRow();
        }
    });
    pushCurrentRow();

    return { hiddenItems, rows };
};

// AI NOTE: KuzhambuForm owns grid layout from container width, not viewport width.
// Only KuzhambuFormItem, KuzhambuFormPlaceholderItem, and hidden items participate in layout.
export const KuzhambuForm = <Values = unknown,>({
    children,
    className,
    colon = true,
    rowGutter = 16,
    style,
    ...formProps
}: KuzhambuFormProps<Values>) => {
    const { containerRef, layoutTier } = useContainerLayoutTier();
    const { hiddenItems, rows } = buildFormRows(children, layoutTier);

    return (
        <div ref={containerRef} className={className} style={style}>
            <Form<Values> {...formProps} colon={colon} layout="horizontal">
                {hiddenItems}
                {rows.map((row, rowIndex) => (
                    <Row key={rowIndex} className="kuzhambu-form-row" gutter={rowGutter}>
                        {row.map((item, itemIndex) => (
                            <Col
                                key={itemIndex}
                                aria-hidden={item.ariaHidden}
                                {...item.colProps}
                                className={`kuzhambu-form-col ${item.colProps.className || ""}`.trim()}
                            >
                                {item.child}
                            </Col>
                        ))}
                    </Row>
                ))}
            </Form>
        </div>
    );
};
