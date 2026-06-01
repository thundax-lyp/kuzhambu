import type { ReactNode } from "react";
import { Typography } from "antd";
import "./sandwish-page.css";

const { Text, Title } = Typography;

interface SandwishPageProps {
    actions?: ReactNode;
    children: ReactNode;
    className?: string;
    description?: ReactNode;
    eyebrow?: ReactNode;
    title: ReactNode;
}

export const SandwishPage = ({
    actions,
    children,
    className,
    description,
    eyebrow,
    title
}: SandwishPageProps) => {
    return (
        <main className={["sandwish-page", className].filter(Boolean).join(" ")}>
            <section className="sandwish-page-panel">
                <header className="sandwish-page-header">
                    <div>
                        {eyebrow ? <Text className="sandwish-page-eyebrow">{eyebrow}</Text> : null}
                        <Title level={2}>{title}</Title>
                        {description ? <Text type="secondary">{description}</Text> : null}
                    </div>
                    {actions ? <div className="sandwish-page-actions">{actions}</div> : null}
                </header>
                {children}
            </section>
        </main>
    );
};
