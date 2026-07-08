# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Portal Web Wangqi QA context`：锁定 Portal 单文档问答上下文
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WANGQI-SINGLE-DOCUMENT-QA.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/pages/discovery/qa-page.tsx`、`kuzhambu-apps/portal-web/src/pages/discovery/qa-page.test.tsx`、`kuzhambu-apps/portal-web/e2e/discovery/qa.spec.ts`
    - 处理动作：展示单文档上下文条、锁定上下文字段、透传首问和追问上下文，并补齐失败重试。
    - 验收点：Portal 单测和 e2e 覆盖 URL 初始化、上下文展示、请求 payload、来源展示、刷新详情和失败重试。
    - 重要度：10/10

- [ ] `main sync`：收口前同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/wangqi-single-doc-qa` 分支
    - 处理动作：在最终收口前从 `main` 同步最新代码并处理冲突。
    - 验收点：功能分支包含最新 `main` 代码且工作区无非任务冲突。
    - 重要度：9/10

- [ ] `Java validation`：执行 Java 后端相关验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WANGQI-SINGLE-DOCUMENT-QA.md`
    - 范围对象：`kuzhambu-servers/biz/discovery`、`kuzhambu-servers/biz/ai`、`kuzhambu-servers/biz/classics/kuzhambu-classics-facade`
    - 处理动作：在同步 `main` 后运行后端 formatter、Spotless、Checkstyle 和相关 Maven 测试。
    - 验收点：Java 格式、静态检查和相关测试通过。
    - 重要度：10/10

- [ ] `Frontend validation`：执行 Admin Web 和 Portal Web 相关验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WANGQI-SINGLE-DOCUMENT-QA.md`
    - 范围对象：`kuzhambu-apps/admin-web`、`kuzhambu-apps/portal-web`
    - 处理动作：在同步 `main` 后运行前端 format、lint、相关单测和 Portal QA e2e。
    - 验收点：前端格式、lint、单测和 `e2e/discovery/qa.spec.ts` 通过。
    - 重要度：10/10

- [ ] `Workers validation`：执行 Discovery AI usecase worker 验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WANGQI-SINGLE-DOCUMENT-QA.md`
    - 范围对象：`kuzhambu-workers`
    - 处理动作：在同步 `main` 后运行 Ruff format、Ruff check 和 Discovery AI usecase pytest。
    - 验收点：Workers 格式、静态检查和 `tests/test_worker_e2e_ai_usecase_discovery.py` 通过。
    - 重要度：8/10

- [ ] `Implementation Coverage sync`：更新实现覆盖率文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-WANGQI-SINGLE-DOCUMENT-QA.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/AI-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：把 Wangqi 单文档问答入口、Discovery AI facade 调用和 AI answer-generation 消费状态同步到 Implementation Coverage。
    - 验收点：三份 coverage 文档反映已实现闭环且无中间状态表述。
    - 重要度：9/10

- [ ] `RUNBOOK cleanup`：任务完成后清理临时 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/30-designs/RUNBOOK-WANGQI-SINGLE-DOCUMENT-QA.md`
    - 处理动作：在功能、验证和 coverage 收口后删除本 RUNBOOK。
    - 验收点：RUNBOOK 文件已删除且无残留引用。
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
