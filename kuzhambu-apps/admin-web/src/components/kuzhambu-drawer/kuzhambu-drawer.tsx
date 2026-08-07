import { Drawer } from "antd";
import type { DrawerProps } from "antd";
import type { ReactNode } from "react";
import { KuzhambuButton } from "@/components/kuzhambu-button";
import "./kuzhambu-drawer.css";

export type KuzhambuDrawerSize = "full" | "large" | "middle" | "small";

export interface KuzhambuDrawerFooterAction {
    action: () => void;
    danger?: boolean;
    disabled?: boolean;
    loading?: boolean;
    testId: string;
    title: ReactNode;
    type?: "default" | "primary";
}

export interface KuzhambuDrawerProps extends Omit<
    DrawerProps,
    "data-testid" | "destroyOnClose" | "footer" | "maskClosable" | "size" | "width"
> {
    footerActions?: KuzhambuDrawerFooterAction[];
    size?: KuzhambuDrawerSize;
    testId: string;
}

const shouldExposeTestId = () => {
    return !import.meta.env.PROD || import.meta.env.VITE_EXPOSE_TEST_ID === "true";
};

// AI NOTE: 这是页面级 Drawer 的唯一入口。
// 下游页面只能通过 footerActions 声明底部按钮，不允许传 Ant Design 的自由 footer JSX；KuzhambuDrawerProps 已 omit footer 以保证编译期约束。
// footerActions 只描述 Button 语义：testId、title、action、type、danger、disabled、loading；布局与按钮组件由这里统一控制。
// 不要在这里加入分段切换或任务工作流行为；这些模式应该放在更高层 wrapper 中。
export const KuzhambuDrawer = ({
    className,
    footerActions,
    placement = "right",
    rootClassName,
    size = "small",
    testId,
    ...drawerProps
}: KuzhambuDrawerProps) => {
    const drawerSize = `var(--kuzhambu-drawer-${size}-width)`;
    const testIdProps = shouldExposeTestId() ? { "data-testid": testId } : {};

    return (
        <Drawer
            {...drawerProps}
            {...testIdProps}
            className={["kuzhambu-drawer", `kuzhambu-drawer-${size}`, className]
                .filter(Boolean)
                .join(" ")}
            footer={
                footerActions?.length ? (
                    <div className="kuzhambu-drawer-footer-actions">
                        {footerActions.map((action) => (
                            <KuzhambuButton
                                key={action.testId}
                                testId={action.testId}
                                type={action.type}
                                danger={action.danger}
                                disabled={action.disabled}
                                loading={action.loading}
                                onClick={action.action}
                            >
                                {action.title}
                            </KuzhambuButton>
                        ))}
                    </div>
                ) : undefined
            }
            placement={placement}
            rootClassName={["kuzhambu-drawer-root", `kuzhambu-drawer-root-${size}`, rootClassName]
                .filter(Boolean)
                .join(" ")}
            size={drawerSize}
        />
    );
};
