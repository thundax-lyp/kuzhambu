import { Input } from "antd";
import { KuzhambuFilterPanel } from "@/components/kuzhambu-filter-panel";
import type { OptionRecord } from "@/types/options";
import { KuzhambuSelect } from "@/components/kuzhambu-select";

export type UserFilterStatus = "ALL" | "ENABLED" | "DISABLED";

export interface UserFilters {
    loginName: string;
    enable: UserFilterStatus;
}

interface UserFilterPanelProps {
    filters: UserFilters;
    loading: boolean;
    open: boolean;
    resetDisabled: boolean;
    statusOptions: OptionRecord[];
    onApply: () => void;
    onFiltersChange: (filters: UserFilters) => void;
    onReset: () => void;
}

export const UserFilterPanel = ({
    filters,
    loading,
    open,
    resetDisabled,
    statusOptions,
    onApply,
    onFiltersChange,
    onReset
}: UserFilterPanelProps) => (
    <KuzhambuFilterPanel
        open={open}
        resetDisabled={resetDisabled}
        fields={[
            {
                name: "loginName",
                label: "登录名",
                render: () => (
                    <Input
                        allowClear
                        placeholder="输入登录名"
                        value={filters.loginName}
                        onChange={(event) =>
                            onFiltersChange({
                                ...filters,
                                loginName: event.target.value
                            })
                        }
                    />
                )
            },
            {
                name: "enable",
                label: "状态",
                render: () => (
                    <KuzhambuSelect<UserFilterStatus>
                        value={filters.enable}
                        options={[
                            { value: "ALL", label: "全部" },
                            ...statusOptions.map((option) => ({
                                value: option.value as UserFilterStatus,
                                label: option.label
                            }))
                        ]}
                        loading={loading}
                        onChange={(enable) =>
                            onFiltersChange({
                                ...filters,
                                enable
                            })
                        }
                    />
                )
            }
        ]}
        onApply={onApply}
        onReset={onReset}
    />
);
