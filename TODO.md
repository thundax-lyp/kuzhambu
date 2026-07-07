# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `04 Admin Web 质量报告重提取服务`：补齐前端调用类型和 API
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-service.test.ts`
    - 处理动作：新增 `ReextractLowQualityCategoryCommand`、`ReextractLowQualityCategoryRecord` 和 `reextractLowQualityCategory` 服务方法。
    - 验收点：服务测试断言 `POST /knowledge/quality/report/reextract-low-quality-category` 的路径、方法和 body 字段完整。
    - 重要度：8/10
- [ ] `05 Admin Web 来源明细重提取控件`：在质量报告来源明细行增加重提取按钮
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/components/quality-report-source-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.css`
    - 处理动作：在来源明细操作列新增 `Tooltip + Button` 的“重提取”控件。
    - 验收点：按钮按报告编号、门类编码、`issueCount` 和 `knowledge:graph:edit` 权限启停，禁用原因清晰，loading 绑定当前来源明细行。
    - 重要度：8/10
- [ ] `06 Admin Web 质量报告重提取编排`：接通确认弹窗、调用和成功反馈
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/quality-report/quality-report-page.test.tsx`
    - 处理动作：在质量报告页接入 `Modal.confirm`、重提取 mutation、页面常量快照和最近创建任务反馈。
    - 验收点：确认后固定提交 `taskType=GRAPH`、`replaceUnconfirmedOnly=true` 和 `QUALITY_REEXTRACT_*` 常量，成功后展示任务号、任务类型、触发来源、批次号和“打开任务台账”按钮。
    - 重要度：9/10
- [ ] `07 Knowledge 低质量门类重提取后端验证`：运行 Knowledge 后端格式、静态和测试检查
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface`
    - 处理动作：运行 RUNBOOK 中 Knowledge 相关 Maven formatter、checkstyle 和测试命令。
    - 验收点：Knowledge 后端格式检查、静态检查和相关测试通过。
    - 重要度：10/10
- [ ] `08 Admin Web 低质量门类重提取前端验证`：运行 Admin Web 格式、lint、测试和构建检查
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`kuzhambu-apps/admin-web`
    - 处理动作：运行 RUNBOOK 中 Admin Web format、lint、test 和 build 命令。
    - 验收点：Admin Web 格式检查、lint、测试和构建通过。
    - 重要度：10/10
- [ ] `09 Knowledge 低质量门类重提取冒烟`：验证质量报告页到任务台账闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`/knowledge/quality-report`、`/knowledge/graph-extraction`
    - 处理动作：手工或自动冒烟验证低质量门类“重提取”创建任务并进入任务台账。
    - 验收点：低质量门类创建出的任务可在任务台账看到 `QUALITY_REPORT`、`selectionScopeJson` 和批次信息。
    - 重要度：10/10
- [ ] `10 Knowledge 重提取分支同步 main`：收口前同步主分支最新代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/knowledge-low-quality-reextract`、`main`
    - 处理动作：在功能、后端验证、前端验证和冒烟通过后，同步最新 `main` 到当前分支并处理冲突。
    - 验收点：当前分支基于最新 `main`，同步后重新确认工作区只包含本任务相关改动。
    - 重要度：9/10
- [ ] `11 Knowledge 同步 main 后回归验证`：同步主分支后重跑受影响验证
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge`、`kuzhambu-apps/admin-web`
    - 处理动作：同步最新 `main` 后重跑 Knowledge 后端和 Admin Web 受影响验证。
    - 验收点：同步 `main` 后没有回归，工作区仍只包含低质量门类重提取闭环相关改动。
    - 重要度：10/10
- [ ] `12 Knowledge Implementation Coverage 与 RUNBOOK 收口`：更新覆盖状态并清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-LOW-QUALITY-REEXTRACT.md`、`TODO.md`
    - 处理动作：将低质量门类一键触发重提取相关覆盖项改为已完成，并删除已完成 TODO 和临时 RUNBOOK。
    - 验收点：Implementation Coverage 不再保留该未完成项或残留风险，RUNBOOK 已删除，`TODO.md` 仅保留真实未关闭任务。
    - 重要度：10/10

## 待讨论项
