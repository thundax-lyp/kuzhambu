import { FilterOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from "@ant-design/icons";
import { Input } from "antd";
import { KuzhambuSpace } from "@/components/kuzhambu-space";
import { KuzhambuButton } from "@/components/kuzhambu-button";

interface UserPageActionsProps {
    canCreateUser: boolean;
    filterActive: boolean;
    filterOpen: boolean;
    isRefreshing: boolean;
    searchText: string;
    onCreate: () => void;
    onRefresh: () => void;
    onSearch: (value: string) => void;
    onToggleFilter: () => void;
}

export const UserPageActions = ({
    canCreateUser,
    filterActive,
    filterOpen,
    isRefreshing,
    searchText,
    onCreate,
    onRefresh,
    onSearch,
    onToggleFilter
}: UserPageActionsProps) => (
    <KuzhambuSpace className="user-page-actions">
        <Input
            allowClear
            aria-label="搜索用户"
            className="user-page-search"
            placeholder="搜索用户..."
            prefix={<SearchOutlined />}
            suffix={<span className="user-page-search-shortcut">⌘K</span>}
            value={searchText}
            onChange={(event) => onSearch(event.target.value)}
        />
        <KuzhambuButton
            testId="system-user-user-page-actions-filter-button"
            className={filterOpen || filterActive ? "user-page-filter-active" : ""}
            icon={<FilterOutlined />}
            aria-expanded={filterOpen}
            onClick={onToggleFilter}
        >
            筛选
        </KuzhambuButton>
        <KuzhambuButton
            testId="system-user-user-page-actions-refresh-button"
            icon={<ReloadOutlined />}
            loading={isRefreshing}
            onClick={onRefresh}
        >
            刷新
        </KuzhambuButton>
        {canCreateUser ? (
            <KuzhambuButton
                testId="system-user-user-page-actions-create-button"
                type="primary"
                icon={<PlusOutlined />}
                onClick={onCreate}
            >
                新增
            </KuzhambuButton>
        ) : null}
    </KuzhambuSpace>
);
