import { Col, Form, Row } from "antd";
import type { ColProps, FormItemProps, FormProps, RowProps } from "antd";
import {
    Children,
    Fragment,
    createContext,
    isValidElement,
    useContext,
    type ReactElement,
    type Key,
    type ReactNode
} from "react";
import {
    KUZHAMBU_FORM_ITEM_LAYOUTS,
    type KuzhambuFormItemLayoutSize,
    type KuzhambuFormLayoutTier
} from "./kuzhambu-form-layout";
import { useKuzhambuFormLayoutTier } from "./hooks/use-kuzhambu-form-layout-tier";

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
}

export type KuzhambuFormHiddenItemProps = Omit<FormItemProps, "hidden" | "labelCol" | "wrapperCol">;

export interface KuzhambuFormPlaceholderItemProps {
    children?: ReactNode;
    colProps?: Omit<ColProps, "lg" | "md" | "offset" | "sm" | "span" | "xl" | "xs" | "xxl">;
    fillLine?: boolean;
    layoutSize?: KuzhambuFormItemLayoutSize;
}

const KuzhambuFormLayoutTierContext = createContext<KuzhambuFormLayoutTier | undefined>(undefined);

export const KuzhambuFormItem = ({
    layoutSize = "middle",
    ...formItemProps
}: KuzhambuFormItemProps) => {
    const layoutTier = useContext(KuzhambuFormLayoutTierContext);
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
    | { element: KuzhambuFormItemElement; key: Key; kind: "item" }
    | { element: KuzhambuFormHiddenItemElement; key: Key; kind: "hidden" }
    | { element: KuzhambuFormPlaceholderItemElement; key: Key; kind: "placeholder" }
    | { child: ReactNode; key: Key; kind: "raw" };

interface FormLayoutItem {
    ariaHidden?: boolean;
    child: ReactNode;
    colProps: ColProps;
    hasChildren?: boolean;
    key: Key;
    kind: "item" | "placeholder" | "raw";
    span: number;
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

const collectLayoutChildren = (
    children: ReactNode,
    keyPrefix = ""
): KuzhambuFormLayoutElement[] => {
    return Children.toArray(children).flatMap((child, index) => {
        const childKey = `${keyPrefix}${isValidElement(child) && child.key !== null ? child.key : index}`;
        if (isFragmentElement(child)) {
            return collectLayoutChildren(child.props.children, `${childKey}/`);
        }
        if (isKuzhambuFormItemElement(child)) {
            return [{ element: child, key: childKey, kind: "item" }];
        }
        if (isKuzhambuFormHiddenItemElement(child)) {
            return [{ element: child, key: childKey, kind: "hidden" }];
        }
        if (isKuzhambuFormPlaceholderItemElement(child)) {
            return [{ element: child, key: childKey, kind: "placeholder" }];
        }
        return [{ child, key: childKey, kind: "raw" }];
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

const isEmptyFullLinePlaceholderRow = (row: FormLayoutItem[]) => {
    return (
        row.length === 1 &&
        row[0].kind === "placeholder" &&
        row[0].span >= 24 &&
        !row[0].hasChildren
    );
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
            currentRow.push({
                ariaHidden: !hasChildren,
                child: hasChildren ? child.props.children : null,
                colProps: readPlaceholderColProps(child.props, currentSpan, layoutTier),
                hasChildren,
                key: layoutChild.key,
                kind: "placeholder",
                span: itemSpan
            });
            currentSpan += itemSpan;
            if (currentSpan >= 24) {
                pushCurrentRow();
            }
            return;
        }

        if (layoutChild.kind === "raw") {
            pushCurrentRow();
            currentRow.push({
                child: layoutChild.child,
                colProps: { span: 24 },
                key: layoutChild.key,
                kind: "raw",
                span: 24
            });
            pushCurrentRow();
            return;
        }

        const child = layoutChild.element;
        const itemSpan = readItemSpan(child.props.layoutSize, layoutTier);
        if (currentRow.length && currentSpan + itemSpan > 24) {
            pushCurrentRow();
        }
        currentRow.push({
            child,
            colProps: readFormItemColProps(child.props, layoutTier),
            key: layoutChild.key,
            kind: "item",
            span: itemSpan
        });
        currentSpan += itemSpan;
        if (currentSpan >= 24) {
            pushCurrentRow();
        }
    });
    pushCurrentRow();

    return { hiddenItems, rows: rows.filter((row) => !isEmptyFullLinePlaceholderRow(row)) };
};

const readRowKey = (row: FormLayoutItem[]) => {
    return row.map((item) => item.key).join("|");
};

// AI NOTE: KuzhambuForm owns grid layout from container width, not viewport width.
// KuzhambuFormItem and KuzhambuFormPlaceholderItem participate in grid layout;
// unknown renderable children are preserved as full-line compatibility content.
export const KuzhambuForm = <Values = unknown,>({
    children,
    className,
    colon = true,
    rowGutter = 16,
    style,
    ...formProps
}: KuzhambuFormProps<Values>) => {
    const { containerRef, layoutTier } = useKuzhambuFormLayoutTier();
    const { hiddenItems, rows } = buildFormRows(children, layoutTier);

    return (
        <div ref={containerRef} className={className} style={style}>
            <KuzhambuFormLayoutTierContext.Provider value={layoutTier}>
                <Form<Values> {...formProps} colon={colon} layout="horizontal">
                    {hiddenItems}
                    {rows.map((row) => (
                        <Row key={readRowKey(row)} className="kuzhambu-form-row" gutter={rowGutter}>
                            {row.map((item) => (
                                <Col
                                    key={item.key}
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
            </KuzhambuFormLayoutTierContext.Provider>
        </div>
    );
};
