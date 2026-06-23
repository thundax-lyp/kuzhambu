# Knowledge Implementation Coverage

## Purpose

本文档记录 Knowledge MVP 当前实现覆盖状态，用于后续补充开发、联调验收和范围控制。

本文档不替代 `docs/10-requirements/KNOWLEDGE-REQUIREMENTS.md`、`docs/30-designs/KNOWLEDGE-DESIGN.md` 或 `docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`。

## Status Definition

- `已完成`：当前仓库已有可追溯交付物，并已形成运行时代码、页面入口或数据种子闭环。
- `部分完成`：已有模型、接口或页面骨架，但仍缺关键联调、验证或端到端闭环。
- `未完成`：当前仓库尚未形成可执行交付物。
- `超出范围`：能力明确被本次 Knowledge MVP 排除，不作为本轮交付目标。

## Current Baseline

已完成：

- Knowledge 已按 `domain -> application -> infra -> interface` 分层落地 taxonomy 子域，标签分类、标签、标签别名、标签内容引用、同义词均已建立独立模型、Repository、Application Service 和 Admin Interface。
- 后端 `/api/knowledge/taxonomy` 已提供标签分类分页/创建/更新/状态变更、标签分页/详情/创建/更新/状态变更、待审核标签分页/审核、标签别名列表/新增/删除、同义词分页/创建/更新/状态变更/删除接口，并补齐 `knowledge:taxonomy:view|edit|review` 权限点。
- Admin Web 已接入 `/knowledge/taxonomy` 页面，支持标签分类分页、创建、编辑、启用、禁用。
- Admin Web 已接入统一标签分页、详情、创建、编辑、启用、禁用，并在详情中展示内容引用数量和内容引用明细。
- Admin Web 已接入待审核标签列表、标签审核抽屉、通过和拒绝动作。
- Admin Web 已接入标签别名列表、新增和删除能力，并复用标签详情抽屉作为治理入口。
- Admin Web 已接入同义词分页、创建、编辑、启用、禁用和删除能力。
- Knowledge 已补充 `KnowledgeTagBindingDomainService`，为 Classics 通用标签提供统一标签解析、手工/AI 标签自动创建、内容引用同步和内容引用删除能力。
- Knowledge taxonomy 已补充与 Classics 协作的兼容口径：接受 `MING_CUSTOMS` 内容类型输入和 `AI` 标签来源输入，并在仓储写入时归一化为内部口径。
- Knowledge 已补齐后端自动化测试，覆盖标签绑定协作语义和 taxonomy 兼容口径。
- `db/data-source/system.json` 与 `db/data/system.sql` 已收敛到本次 MVP 指定的 `知识治理 / 标签与同义词` 菜单结构，并通过现有脚本重新生成。

部分完成：

- 当前已完成页面级 format/lint、菜单 SQL 生成校验和 taxonomy 后端专项测试，但尚未补充前端单测、Playwright 闭环或联调冒烟记录。

未完成：

- 无。当前 TODO 范围内的 MVP 交付项已全部落地到仓库。

## Requirement Coverage Matrix

| 需求项 | 状态 | 已完成部分 | 未完成部分 | 责任域 |
| --- | --- | --- | --- | --- |
| 标签分类管理 | 已完成 | 后端已提供分页、创建、更新、状态变更接口；Admin Web 已提供分类列表、搜索、新增、编辑、启用、禁用 | 无 | Knowledge, Admin Web |
| 统一标签管理 | 已完成 | 后端已提供标签分页、详情、创建、更新、状态变更接口；Admin Web 已提供列表、详情抽屉、新增、编辑、启用、禁用、内容引用数量展示 | 无 | Knowledge, Admin Web |
| 标签待审核列表与审核 | 已完成 | 后端已提供待审核分页和审核接口；Admin Web 已提供待审核列表、审核抽屉、通过/拒绝动作 | 无 | Knowledge, Admin Web |
| 标签别名管理 | 已完成 | 后端已提供别名列表、新增、删除接口；Admin Web 已在标签详情抽屉中提供别名列表、新增和删除 | 无 | Knowledge, Admin Web |
| 同义词管理 | 已完成 | 后端已提供分页、创建、更新、状态变更、删除接口；Admin Web 已提供分页表格、编辑抽屉、启用、禁用、删除 | 无 | Knowledge, Admin Web |
| Classics 内容标签协作与兼容口径 | 已完成 | Knowledge 已提供统一标签绑定协作语义、内容引用同步/删除能力，并兼容 Classics 的 `MING_CUSTOMS` 与 `AI` 协作输入 | 无 | Knowledge, Classics |
| 后台菜单与页面入口 | 已完成 | `system.json` 已新增 `知识治理 / 标签与同义词` 菜单；`system.sql` 已由脚本生成同步；Admin Web 路由已接入 `/knowledge/taxonomy` | 无 | System Data, Admin Web |
| taxonomy 权限点 | 已完成 | `knowledge:taxonomy:view`、`knowledge:taxonomy:edit`、`knowledge:taxonomy:review` 已进入菜单种子和后端 `@HasPermission` 控制 | 无 | Knowledge, System |
| MVP 运行时验证 | 部分完成 | 已完成 taxonomy 后端专项测试、前端 `format`、`lint` 以及 `node scripts/generate-system-data-sql.ts --check` | 缺少前端单测、Playwright 与联调冒烟记录 | Knowledge, Admin Web |

## Out Of Scope Matrix

| 能力项 | 状态 | 说明 |
| --- | --- | --- |
| 数据精修 | 超出范围 | RUNBOOK 已明确禁止本次实现 |
| 知识图谱 | 超出范围 | RUNBOOK 已明确禁止本次实现 |
| 世系图 | 超出范围 | RUNBOOK 已明确禁止本次实现 |
| Portal 页面 | 超出范围 | Knowledge MVP 仅交付 Admin 端 |
| Workers 改造 | 超出范围 | 本轮不接 Discovery/Worker 链路 |
| Discovery 搜索或问答接入 | 超出范围 | 仅保留 taxonomy 治理闭环 |
| 标签合并、标签废弃、批量操作、统计报表 | 超出范围 | 本轮不扩展治理动作 |
| Classics 内容编辑页内联知识治理入口 | 超出范围 | 本轮只提供独立 taxonomy 页面 |

## Residual Risks

- 菜单种子重生成后 `system_menu` 的树编号和自增值已随节点数收缩变化，后续若依赖固定菜单 ID，需要以当前生成结果为准重新校对。
- taxonomy 页面目前以页面级查询和抽屉交互为主，尚未形成自动化 UI 回归覆盖；后续再改权限、字段或接口返回时，建议补前端契约测试和 Playwright 冒烟。
