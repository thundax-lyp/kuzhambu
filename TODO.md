# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Sancai Asset Frontend Contract`：补齐三才图会视觉资产前端契约与 service
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-service-contract.test.ts`
    - 处理动作：新增 `SancaiVisualAssetRecord`、更新与切换 command 类型，以及视觉资产 service 方法和请求路径断言
    - 验收点：存在 `listVisualAssets`、`updateVisualAsset`、`useVisualAsset` 三个 service 方法，且 contract test 固定 `visual-assets` 路径与请求体
    - 重要度：9/10

- [ ] `Sancai Asset Model UI`：在条目详情弹窗接入视觉资产展示区块
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-types.ts`
    - 处理动作：在条目详情弹窗中新增视觉资产展示结构，呈现当前使用版本、历史列表、原图/生成图预览下载和基础字段查看区
    - 验收点：打开三才条目时可看到视觉资产区块、当前使用版本、原图/生成图预览下载入口
    - 重要度：9/10

- [ ] `Sancai Asset Panel Actions`：接通视觉资产切换与保存交互
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/services/sancai-entry-service.ts`
    - 处理动作：把“设为当前使用版本”和基础字段保存动作接到 service，确保弹窗和列表刷新联动
    - 验收点：用户可以切换当前视觉资产版本并看到刷新结果，基础字段保存走正式 service 契约
    - 重要度：9/10

- [ ] `Sancai Asset Component Tests`：补齐视觉资产组件级测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-model.tsx`
    - 处理动作：补齐视觉资产区块展示、切换、原图/生成图入口和字段展示的组件级测试
    - 验收点：组件测试覆盖视觉资产核心 UI 和切换回调，不依赖新增独立页面
    - 重要度：8/10

- [ ] `Sancai Asset Page Regression`：收口三才页面层回归测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/classics/sancai/sancai-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/classics/sancai/components/sancai-entry-panel.tsx`
    - 处理动作：校准页面级回归测试，确保视觉资产区块接入后不破坏原有三才条目打开、保存、导出和展示流程
    - 验收点：页面级测试通过，且不新增独立视觉资产页面
    - 重要度：8/10

- [ ] `Sancai Coverage And Closure`：更新覆盖文档并完成任务收口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`
    - 范围对象：`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-SANCAI-VISUAL-ASSETS-CLOSURE.md`、`TODO.md`
    - 处理动作：更新三才视觉资产相关 Implementation Coverage 口径，删除已完成 RUNBOOK，并将 TODO 收窄为剩余未完成内容或清空
    - 验收点：`CLASSICS-IMPLEMENTATION-COVERAGE.md` 已同步当前实现状态，RUNBOOK 已删除，`TODO.md` 不保留已完成任务
    - 重要度：8/10

- [ ] `Sancai Final Verify`：执行全量格式化、静态检查、构建与测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：按仓库规则执行本次改动涉及模块的格式化与最终校验，包括 servers 的 `spotless/checkstyle/test` 和 apps 的 `format:check/lint/build/test`
    - 验收点：至少完成 `cd kuzhambu-servers && mvn -q spotless:check && mvn -q checkstyle:check && mvn -q test` 与 `cd kuzhambu-apps && npm run format:check && npm run lint && npm run build && npm test`，结果可用于 PR 描述
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
