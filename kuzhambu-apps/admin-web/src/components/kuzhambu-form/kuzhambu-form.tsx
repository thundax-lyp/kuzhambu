import { Col, Form, Row } from "antd";
import type { ColProps, FormItemProps, FormProps, RowProps } from "antd";
import { Fragment, type ReactNode } from "react";
import {
    KUZHAMBU_FORM_ITEM_LAYOUTS,
    KUZHAMBU_FORM_ITEM_OFFSETS,
    type KuzhambuFormItemLayoutSize,
    type KuzhambuFormItemOffsetSize
} from "./kuzhambu-form-layout";

export interface KuzhambuFormProps<Values = unknown> extends Omit<FormProps<Values>, "layout"> {
    rowGutter?: RowProps["gutter"];
}

export interface KuzhambuFormItemProps extends Omit<FormItemProps, "labelCol" | "wrapperCol"> {
    colProps?: Omit<ColProps, "lg" | "md" | "offset" | "sm" | "span" | "xl" | "xs" | "xxl">;
    endOfLine?: boolean;
    layoutSize?: KuzhambuFormItemLayoutSize;
    offsetSize?: KuzhambuFormItemOffsetSize;
}

// AI NOTE: KuzhambuForm owns only the shared grid container and horizontal label mode.
// Field rendering, validation, values, and submission remain plain Ant Design Form behavior.
export const KuzhambuForm = <Values = unknown,>({
    children,
    colon = true,
    rowGutter = 16,
    ...formProps
}: KuzhambuFormProps<Values>) => {
    return (
        <Form<Values> {...formProps} colon={colon} layout="horizontal">
            <Row gutter={rowGutter}>{children as ReactNode}</Row>
        </Form>
    );
};

export const KuzhambuFormItem = ({
    colProps,
    endOfLine = false,
    layoutSize = "middle",
    offsetSize,
    ...formItemProps
}: KuzhambuFormItemProps) => {
    const layout = KUZHAMBU_FORM_ITEM_LAYOUTS[layoutSize];
    const offset = offsetSize ? KUZHAMBU_FORM_ITEM_OFFSETS[offsetSize] : undefined;
    const md = offset ? { span: layout.col.md, offset: offset.md } : layout.col.md;
    const lg = offset ? { span: layout.col.lg, offset: offset.lg } : layout.col.lg;

    return (
        <Fragment>
            <Col {...colProps} xs={layout.col.xs} md={md} lg={lg}>
                <Form.Item
                    {...formItemProps}
                    labelCol={layout.labelCol}
                    wrapperCol={layout.wrapperCol}
                />
            </Col>
            {endOfLine ? <Col aria-hidden xs={24} md={24} lg={24} /> : null}
        </Fragment>
    );
};
