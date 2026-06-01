import { MenuOutlined, PlusOutlined, ReloadOutlined } from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Space, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { SandwishListPage } from "@/components/sandwish-list-page";
import { useKuzhambuConfirm } from "@/components/kuzhambu-confirm-modal/hooks/use-kuzhambu-confirm";
import { SandwishSwitch } from "@/components/sandwish-switch";
import type { KuzhambuTableProps, KuzhambuTableSortPosition } from "@/components/kuzhambu-table";
import { MenuEdit } from "./components/menu-edit";
import * as service from "./menu-service";
import type { MenuMoveCommand, MenuSaveCommand } from "./menu-service";
import type { MenuNode, MenuTableNode } from "./menu-types";
import "./menu-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    url: 260,
    perms: 240,
    display: 96
};

const buildMenuTree = (menus: MenuNode[]) => {
    const nodeMap = new Map<string, MenuTableNode>();
    const roots: MenuTableNode[] = [];

    menus.forEach((menu) => {
        nodeMap.set(menu.id, { ...menu });
    });

    nodeMap.forEach((menu) => {
        if (menu.parentId) {
            const parent = nodeMap.get(menu.parentId);
            if (parent) {
                parent.children = parent.children || [];
                parent.children.push(menu);
                return;
            }
        }
        roots.push(menu);
    });

    return roots;
};

const collectMenuIds = (menus: MenuTableNode[]): string[] => {
    return menus.flatMap((menu) => [
        menu.id,
        ...(menu.children ? collectMenuIds(menu.children) : [])
    ]);
};

const flattenMenus = (menus: MenuTableNode[]): MenuTableNode[] => {
    return menus.flatMap((menu) => [menu, ...(menu.children ? flattenMenus(menu.children) : [])]);
};

const collectDescendantIds = (menu?: MenuTableNode | null): Set<string> => {
    if (!menu?.children?.length) {
        return new Set();
    }
    return new Set(collectMenuIds(menu.children));
};

const toMoveType = (position: KuzhambuTableSortPosition): MenuMoveCommand["type"] => {
    return position === "before" ? "before" : "after";
};

export const MenuPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useKuzhambuConfirm();
    const queryClient = useQueryClient();
    const [editingMenu, setEditingMenu] = useState<MenuTableNode | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const [expandedRowKeys, setExpandedRowKeys] = useState<Key[] | null>(null);
    const canEditMenu = hasPermission("super");
    const menuQuery = useQuery({
        queryKey: ["menu", "list"],
        queryFn: () => service.listMenus(),
        retry: false
    });
    const menus = useMemo(() => menuQuery.data || [], [menuQuery.data]);
    const menuTree = useMemo(() => buildMenuTree(menus), [menus]);
    const flatMenus = useMemo(() => flattenMenus(menuTree), [menuTree]);
    const expandedMenuIds = useMemo(() => collectMenuIds(menuTree), [menuTree]);
    const actualExpandedRowKeys = expandedRowKeys || expandedMenuIds;
    const unavailableParentIds = useMemo(() => {
        const descendantIds = collectDescendantIds(editingMenu);
        if (editingMenu?.id) {
            descendantIds.add(editingMenu.id);
        }
        return descendantIds;
    }, [editingMenu]);
    const parentOptions = useMemo(
        () =>
            flatMenus
                .filter((menu) => !unavailableParentIds.has(menu.id))
                .map((menu) => ({
                    label: menu.name,
                    value: menu.id
                })),
        [flatMenus, unavailableParentIds]
    );

    const saveMutation = useMutation({
        mutationFn: (values: MenuSaveCommand) =>
            values.id ? service.changeMenuInfo(values) : service.addMenu(values),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingMenu(null);
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: service.removeMenus,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const moveMutation = useMutation({
        mutationFn: service.moveMenu,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单层级已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "移动失败");
        }
    });

    const displayMutation = useMutation({
        mutationFn: ({ id, display }: { id: string; display: boolean }) =>
            service.changeMenuDisplay(id, display),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单显示状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "显示状态更新失败");
        }
    });

    const openCreateEditor = () => {
        setEditingMenu(null);
        setEditorOpen(true);
    };

    const openEditEditor = (menu: MenuTableNode) => {
        setEditingMenu(menu);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingMenu(null);
    };

    const saveMenu = (request: MenuSaveCommand) => {
        saveMutation.mutate(request);
    };

    const openDeleteConfirm = (menu: MenuTableNode) => {
        confirm.danger({
            title: "删除菜单",
            message: `确认删除 ${menu.name || ""}？`,
            description: "删除后需要重新新增。若该菜单下仍有关联子菜单，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync([menu.id])
        });
    };

    const sortMenu = (
        sourceMenu: MenuTableNode,
        targetMenu: MenuTableNode,
        position: KuzhambuTableSortPosition
    ) => {
        if (!canEditMenu || sourceMenu.id === targetMenu.id) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: sourceMenu.id,
            toNodeId: targetMenu.id,
            type: toMoveType(position)
        });
    };

    const readSiblingMenus = (menu: MenuTableNode) => {
        if (!menu.parentId) {
            return menuTree;
        }
        return flatMenus.find((item) => item.id === menu.parentId)?.children || [];
    };

    const readPreviousSiblingMenu = (menu: MenuTableNode) => {
        const siblings = readSiblingMenus(menu);
        const index = siblings.findIndex((item) => item.id === menu.id);
        return index > 0 ? siblings[index - 1] : null;
    };

    const promoteMenu = (menu: MenuTableNode) => {
        if (!canEditMenu || !menu.parentId) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: menu.id,
            toNodeId: menu.parentId,
            type: "after"
        });
    };

    const demoteMenu = (menu: MenuTableNode) => {
        const previousSibling = readPreviousSiblingMenu(menu);
        if (!canEditMenu || !previousSibling) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: menu.id,
            toNodeId: previousSibling.id,
            type: "insideLast"
        });
    };

    const columns: KuzhambuTableProps<MenuTableNode>["columns"] = [
        {
            title: "菜单名称",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            ellipsis: true,
            render: (name: string) => (
                <Space size={8} className="menu-name-cell">
                    <MenuOutlined className="menu-name-icon" />
                    <span className="menu-name-text" title={name}>
                        {name}
                    </span>
                </Space>
            )
        },
        {
            title: "URL",
            dataIndex: "url",
            key: "url",
            width: DEFAULT_COLUMN_WIDTHS.url,
            ellipsis: true,
            render: (url?: string | null) =>
                url ? (
                    <span className="menu-path-text" title={url}>
                        {url}
                    </span>
                ) : null
        },
        {
            title: "权限标识",
            dataIndex: "perms",
            key: "perms",
            width: DEFAULT_COLUMN_WIDTHS.perms,
            ellipsis: true,
            render: (perms?: string | null) =>
                perms ? (
                    <span className="menu-path-text" title={perms}>
                        {perms}
                    </span>
                ) : null
        },
        {
            title: "显示",
            dataIndex: "display",
            key: "display",
            width: DEFAULT_COLUMN_WIDTHS.display,
            render: (display: boolean | null | undefined, menu) => (
                <SandwishSwitch
                    checked={display !== false}
                    checkedChildren="显示"
                    unCheckedChildren="隐藏"
                    disabled={!canEditMenu || displayMutation.isPending}
                    aria-label={`切换 ${menu.name} 显示状态，当前${display === false ? "隐藏" : "显示"}`}
                    onChange={(checked) =>
                        displayMutation.mutate({ id: menu.id, display: checked })
                    }
                />
            )
        },
        {
            key: "actions",
            options: (menu) => [
                {
                    key: "promote",
                    text: "升级",
                    ariaLabel: `升级 ${menu.name}`,
                    disabled: !canEditMenu || !menu.parentId || moveMutation.isPending,
                    onClick: () => promoteMenu(menu)
                },
                {
                    key: "demote",
                    text: "降级",
                    ariaLabel: `降级 ${menu.name}`,
                    disabled:
                        !canEditMenu || !readPreviousSiblingMenu(menu) || moveMutation.isPending,
                    onClick: () => demoteMenu(menu)
                },
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${menu.name}`,
                    disabled: !canEditMenu,
                    onClick: () => openEditEditor(menu)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${menu.name}`,
                    disabled: !canEditMenu,
                    onClick: () => openDeleteConfirm(menu)
                }
            ]
        }
    ];

    return (
        <>
            <SandwishListPage<MenuTableNode>
                pageClassName="menu-page"
                title="菜单管理"
                description="维护后台菜单树、页面入口和权限标识。"
                subjectName="菜单"
                pageActions={
                    <>
                        <Button icon={<ReloadOutlined />} onClick={() => menuQuery.refetch()}>
                            刷新
                        </Button>
                        {canEditMenu ? (
                            <Button
                                type="primary"
                                icon={<PlusOutlined />}
                                onClick={openCreateEditor}
                            >
                                新增菜单
                            </Button>
                        ) : null}
                    </>
                }
                rowKey="id"
                className="menu-table"
                columns={columns}
                dataSource={menuTree}
                loading={menuQuery.isFetching || moveMutation.isPending}
                pagination={false}
                scroll={{ x: 1064 }}
                expandable={{
                    defaultExpandAllRows: true,
                    expandedRowKeys: actualExpandedRowKeys,
                    indentSize: 24,
                    expandIconColumnIndex: 0,
                    onExpandedRowsChange: (keys) => setExpandedRowKeys([...keys])
                }}
                locale={{
                    emptyText: menuQuery.isError ? (
                        "菜单列表加载失败，请确认权限和接口状态。"
                    ) : (
                        <Space orientation="vertical" size={8}>
                            <MenuOutlined className="menu-empty-icon" />
                            <Text type="secondary">暂无菜单数据</Text>
                        </Space>
                    )
                }}
                onSort={sortMenu}
                sortable={canEditMenu}
            />

            <MenuEdit
                open={editorOpen}
                menu={editingMenu}
                parentOptions={parentOptions}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={saveMenu}
            />
        </>
    );
};
