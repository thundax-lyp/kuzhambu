import type { ReactNode } from "react";
import { Typography } from "antd";
import "./kuzhambu-page.css";

const { Text, Title } = Typography;

interface KuzhambuPageProps {
    actions?: ReactNode;
    children: ReactNode;
    className?: string;
    description?: ReactNode;
    title: ReactNode;
}

// AI NOTE: This is the base admin page frame.
// Use it to keep title, description, actions, and content spacing consistent.
// Do not move page-specific fetches, permissions, or workflow state into this frame.
export const KuzhambuPage = ({
    actions,
    children,
    className,
    description,
    title
}: KuzhambuPageProps) => {
    return (
        <main className={["kuzhambu-page", className].filter(Boolean).join(" ")}>
            <header className="kuzhambu-page-header">
                <div>
                    <Title level={2}>{title}</Title>
                    {description ? <Text type="secondary">{description}</Text> : null}
                </div>
                {actions ? <div className="kuzhambu-page-actions">{actions}</div> : null}
            </header>
            {children}
        </main>
    );
};
