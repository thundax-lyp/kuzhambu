import { Navigate, RouterProvider, createBrowserRouter } from "react-router-dom";
import { AdminLayout } from "../layouts/admin-layout";
import { InvocationPage } from "../pages/ai/invocation/invocation-page";
import { AiModelPage } from "../pages/ai/ai-model/ai-model-page";
import { BusinessConfigPage } from "../pages/ai/business-config/business-config-page";
import { PromptPage } from "../pages/ai/prompt/prompt-page";
import { AuditLogPage } from "../pages/audit/audit-log/audit-log-page";
import { LoginPage } from "../pages/auth/login/login-page";
import { QaPage } from "../pages/discovery/qa/qa-page";
import { QaConsolePage } from "../pages/discovery/qa-console/qa-console-page";
import { SearchPage } from "../pages/discovery/search/search-page";
import { SearchStatisticPage } from "../pages/discovery/search-statistic/search-statistic-page";
import { MingCustomPage } from "../pages/classics/ming-custom/ming-custom-page";
import { SancaiPage } from "../pages/classics/sancai/sancai-page";
import { SancaiVisualPage } from "../pages/classics/sancai-visual/sancai-visual-page";
import { WangqiPage } from "../pages/classics/wangqi/wangqi-page";
import { PublicationJobPage } from "../pages/classics/publication-job/publication-job-page";
import { DashboardPage } from "../pages/dashboard/dashboard/dashboard-page";
import { GraphExtractionPage } from "../pages/knowledge/graph-extraction/graph-extraction-page";
import { GraphWorkbenchPage } from "../pages/knowledge/graph-workbench/graph-workbench-page";
import { GraphMaterialPage } from "../pages/knowledge/graph-material/graph-material-page";
import { GraphGovernancePage } from "../pages/knowledge/graph-governance/graph-governance-page";
import { GraphDeletionChangePage } from "../pages/knowledge/graph-deletion-change/graph-deletion-change-page";
import { GraphDeletionTaskPage } from "../pages/knowledge/graph-deletion-task/graph-deletion-task-page";
import { LineagePage } from "../pages/knowledge/lineage/lineage-page";
import { GraphResultPage } from "../pages/knowledge/graph-result/graph-result-page";
import { QualityReportPage } from "../pages/knowledge/quality-report/quality-report-page";
import { BackupRestorePage } from "../pages/operations/backup-restore/backup-restore-page";
import { CleanupPage } from "../pages/operations/cleanup/cleanup-page";
import { OperationsDashboardPage } from "../pages/operations/dashboard/dashboard-page";
import { OperationsHealthPage } from "../pages/operations/health/health-page";
import { OperationsReportPage } from "../pages/operations/report/report-page";
import { OperationsTaskPage } from "../pages/operations/task/task-page";
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
                            path: "ai/models",
                            element: <AiModelPage />
                        },
                        {
                            path: "ai/prompts",
                            element: <PromptPage />
                        },
                        {
                            path: "ai/business-configs",
                            element: <BusinessConfigPage />
                        },
                        {
                            path: "ai/invocations",
                            element: <InvocationPage />
                        },
                        {
                            path: "classics/sancai",
                            element: <SancaiPage />
                        },
                        {
                            path: "classics/sancai/visual",
                            element: <SancaiVisualPage />
                        },
                        {
                            path: "classics/ming-customs",
                            element: <MingCustomPage />
                        },
                        {
                            path: "classics/wangqi",
                            element: <WangqiPage />
                        },
                        {
                            path: "classics/publication-jobs",
                            element: <PublicationJobPage />
                        },
                        {
                            path: "knowledge/graph",
                            element: <Navigate to="/knowledge/graph-material" replace />
                        },
                        {
                            path: "knowledge/graph-workbench",
                            element: <GraphWorkbenchPage />
                        },
                        {
                            path: "knowledge/graph-governance",
                            element: <GraphGovernancePage />
                        },
                        {
                            path: "knowledge/graph-material",
                            element: <GraphMaterialPage />
                        },
                        {
                            path: "knowledge/graph-deletion-changes",
                            element: <GraphDeletionChangePage />
                        },
                        {
                            path: "knowledge/graph-deletion-tasks",
                            element: <GraphDeletionTaskPage />
                        },
                        {
                            path: "knowledge/graph-extraction",
                            element: <GraphExtractionPage />
                        },
                        {
                            path: "knowledge/graph-results",
                            element: <GraphResultPage />
                        },
                        {
                            path: "knowledge/lineage",
                            element: <LineagePage />
                        },
                        {
                            path: "knowledge/quality-report",
                            element: <QualityReportPage />
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
                            path: "discovery/search",
                            element: <SearchPage />
                        },
                        {
                            path: "discovery/qa-admin",
                            element: <Navigate to="/discovery/qa-console" replace />
                        },
                        {
                            path: "discovery/qa-console",
                            element: <QaConsolePage />
                        },
                        {
                            path: "discovery/qa",
                            element: <QaPage />
                        },
                        {
                            path: "discovery/search-statistics",
                            element: <SearchStatisticPage />
                        },
                        {
                            path: "operations/dashboard",
                            element: <OperationsDashboardPage />
                        },
                        {
                            path: "operations/health",
                            element: <OperationsHealthPage />
                        },
                        {
                            path: "operations/tasks",
                            element: <OperationsTaskPage />
                        },
                        {
                            path: "operations/reports",
                            element: <OperationsReportPage />
                        },
                        {
                            path: "operations/backup-restore",
                            element: <BackupRestorePage />
                        },
                        {
                            path: "operations/cleanup",
                            element: <CleanupPage />
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
