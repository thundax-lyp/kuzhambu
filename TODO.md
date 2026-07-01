# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [x] `sancai/image-gen-capability`：补齐生图能力入口与 capability 识别
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSET-WORKFLOW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/hooks/use-sancai-entry-panel-state.ts`、`kuzhambu-workers/src/kuzhambu_workers/schemas/ai.py`、`kuzhambu-workers/src/kuzhambu_workers/ai/usecase_registry.py`
    - 处理动作：补齐 `image_gen` capability 的前端入口和 workers 识别
    - 验收点：页面可发起 `image_gen`，workers 枚举和注册口径支持该能力
    - 重要度：8/10

- [ ] `sancai/image-gen-storage`：接通生图产物转存与新版本规则
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSET-WORKFLOW-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/sancai/service/impl/SancaiAssetApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/sancai/repository/SancaiAssetRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java`、`kuzhambu-workers/src/kuzhambu_workers/api/artifact_routes.py`、`kuzhambu-workers/src/kuzhambu_workers/render/artifact_store.py`
    - 处理动作：将 `image_gen` artifact 转存到 Storage 并默认生成新的 visual asset version
    - 验收点：页面事实只基于 Storage 对象，新生成图不会直接覆盖已有生成图版本
    - 重要度：10/10

- [ ] `sancai/image-gen-delivery`：补齐生图预览下载链路
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSET-WORKFLOW-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/SancaiAssetAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/controller/response/SancaiAssetResponse.java`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
    - 处理动作：补齐生成图在页面内的正式预览和下载入口
    - 验收点：生成图在条目详情中可预览、可下载，且都基于已转存的 Storage 对象
    - 重要度：9/10

- [ ] `sancai/visual-workflow-tests`：补齐视觉资产工作流核心测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSET-WORKFLOW-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/sancai/SancaiAssetAdminControllerTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/sancai/SancaiAssetApplicationServiceImplTest.java`、`kuzhambu-workers/tests/test_artifact_store.py`、`kuzhambu-workers/tests/test_ai_usecase_routes_classics.py`
    - 处理动作：为视觉资产工作流补齐面板、controller、应用服务、artifact store 和 workers 契约测试
    - 验收点：视觉资产闭环关键路径具备自动化测试保护
    - 重要度：10/10

- [ ] `classics/coverage-and-cleanup`：更新覆盖文档并删除本轮 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSET-WORKFLOW-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSET-WORKFLOW-CLOSURE.md`
    - 处理动作：按真实完成度更新 Implementation Coverage 并在收口时删除本轮 RUNBOOK
    - 验收点：coverage 口径与实际交付一致，RUNBOOK 不再保留残留引用
    - 重要度：9/10

- [ ] `repo/final-verification`：执行最终全量格式化与校验收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`、`kuzhambu-workers/`
    - 处理动作：按仓库治理要求完成最终全量 `spotless:apply/checkstyle/test`、`format/lint/build/test`、`ruff format/check/pytest` 校验
    - 验收点：后端、前端、workers 的最终全量格式化和验证命令全部通过
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
