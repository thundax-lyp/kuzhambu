import { Navigate, RouterProvider, createBrowserRouter } from "react-router-dom";
import { AdminLayout } from "../layouts/admin-layout";
import { AuditLogPage } from "../pages/audit/audit-log/audit-log-page";
import { LoginPage } from "../pages/auth/login/login-page";
import { QaAdminPage } from "../pages/discovery/qa-admin/qa-admin-page";
import { SearchAdminPage } from "../pages/discovery/search-admin/search-admin-page";
import { MingCustomsPage } from "../pages/classics/ming-customs/ming-customs-page";
import { SancaiPage } from "../pages/classics/sancai/sancai-page";
import { WangqiPage } from "../pages/classics/wangqi/wangqi-page";
import { SharingPage } from "../pages/classics/sharing/sharing-page";
import { DashboardPage } from "../pages/dashboard/dashboard/dashboard-page";
import { GraphExtractionPage } from "../pages/knowledge/graph-extraction/graph-extraction-page";
import { GraphResultsPage } from "../pages/knowledge/graph-results/graph-results-page";
import { BackupRestorePage } from "../pages/operations/backup-restore/backup-restore-page";
import { OperationsTasksPage } from "../pages/operations/tasks/tasks-page";
import { RefinementPage } from "../pages/knowledge/refinement/refinement-page";
import { TaxonomyPage } from "../pages/knowledge/taxonomy/taxonomy-page";
import { StorageObjectPage } from "../pages/storage/storage-object/storage-object-page";
import { DepartmentPage } from "../pages/system/department/department-page";
import { DictionaryPage } from "../pages/system/dictionary/dictionary-page";
import { MenuPage } from "../pages/system/menu/menu-page";
import { RolePage } from "../pages/system/role/role-page";
import { SystemLogPage } from "../pages/system/system-log/system-log-page";
import { UserPage } from "../pages/system/user/user-page";
import { ProtectedRoute } from "./protected-route";

const normalizeRouterBasename = (baseUrl: string) => {
    const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");
    return normalizedBaseUrl === "" ? undefined : normalizedBaseUrl;
};

const router = createBrowserRouter(
    [
        {
            path: "/login",
            element: <LoginPage />
        },
        {
            path: "/",
            element: <ProtectedRoute />,
            children: [
                {
                    element: <AdminLayout />,
                    children: [
                        {
                            index: true,
                            element: <Navigate to="/dashboard" replace />
                        },
                        {
                            path: "dashboard",
                            element: <DashboardPage />
                        },
                        {
                            path: "system/users",
                            element: <UserPage />
                        },
                        {
                            path: "system/departments",
                            element: <DepartmentPage />
                        },
                        {
                            path: "system/roles",
                            element: <RolePage />
                        },
                        {
                            path: "system/menus",
                            element: <MenuPage />
                        },
                        {
                            path: "system/dictionaries",
                            element: <DictionaryPage />
                        },
                        {
                            path: "system/logs",
                            element: <SystemLogPage />
                        },
                        {
                            path: "storage/objects",
                            element: <StorageObjectPage />
                        },
                        {
                            path: "audit/logs",
                            element: <AuditLogPage />
                        },
                        {
                            path: "classics/sancai",
                            element: <SancaiPage />
                        },
                        {
                            path: "classics/ming-customs",
                            element: <MingCustomsPage />
                        },
                        {
                            path: "classics/wangqi",
                            element: <WangqiPage />
                        },
                        {
                            path: "classics/sharing",
                            element: <SharingPage />
                        },
                        {
                            path: "knowledge/graph-extraction",
                            element: <GraphExtractionPage />
                        },
                        {
                            path: "knowledge/graph-results",
                            element: <GraphResultsPage />
                        },
                        {
                            path: "knowledge/refinement",
                            element: <RefinementPage />
                        },
                        {
                            path: "knowledge/taxonomy",
                            element: <TaxonomyPage />
                        },
                        {
                            path: "discovery/qa-admin",
                            element: <QaAdminPage />
                        },
                        {
                            path: "discovery/search-admin",
                            element: <SearchAdminPage />
                        },
                        {
                            path: "operations/tasks",
                            element: <OperationsTasksPage />
                        },
                        {
                            path: "operations/backup-restore",
                            element: <BackupRestorePage />
                        }
                    ]
                }
            ]
        }
    ],
    {
        basename: normalizeRouterBasename(import.meta.env.BASE_URL)
    }
);

export const AppRouter = () => {
    return <RouterProvider router={router} />;
};
