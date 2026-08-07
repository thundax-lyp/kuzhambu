import { Typography } from "antd";
import type { ComponentProps } from "react";

type TextProps = ComponentProps<typeof Typography.Text>;
type TitleProps = ComponentProps<typeof Typography.Title>;
type ParagraphProps = ComponentProps<typeof Typography.Paragraph>;
type LinkProps = ComponentProps<typeof Typography.Link>;

const joinClassNames = (...classNames: Array<string | undefined>) =>
    classNames.filter(Boolean).join(" ");

// AI NOTE: Thin Typography boundary. Keep semantic text choices in callers and
// move shared antd Typography selector work here only when it is component-wide.
export const KuzhambuText = ({ className, ...props }: TextProps) => (
    <Typography.Text {...props} className={joinClassNames("kuzhambu-typography-text", className)} />
);

export const KuzhambuTitle = ({ className, ...props }: TitleProps) => (
    <Typography.Title
        {...props}
        className={joinClassNames("kuzhambu-typography-title", className)}
    />
);

export const KuzhambuParagraph = ({ className, ...props }: ParagraphProps) => (
    <Typography.Paragraph
        {...props}
        className={joinClassNames("kuzhambu-typography-paragraph", className)}
    />
);

export const KuzhambuTypographyLink = ({ className, ...props }: LinkProps) => (
    <Typography.Link {...props} className={joinClassNames("kuzhambu-typography-link", className)} />
);
