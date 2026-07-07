# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Portal lineage page`：11 实现 Portal 只读世系页面
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`
    - 范围对象：`kuzhambu-apps/portal-web/src/app.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.css`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.test.tsx`
    - 处理动作：新增 `/knowledge/lineage` 页面和只读交互。
    - 验收点：Portal 页面支持版本、搜索、节点类型、关系类型、清除筛选和只读详情，不出现后台操作按钮。
    - 重要度：10/10

- [ ] `Portal knowledge home`：12 接入 Portal 首页世系入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/portal/KnowledgePortalReadApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/home/controller/KnowledgePortalHomeControllerTest.java`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-home-page.test.tsx`
    - 处理动作：把 Knowledge 首页世系入口指向 `/knowledge/lineage`。
    - 验收点：首页可见「世系图浏览」入口，点击进入独立世系页面而不是 `/knowledge/atlas`。
    - 重要度：8/10

- [ ] `Backend lineage tests`：13 补齐后端世系接口和用例测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/lineage/KnowledgeLineageReadApplicationServiceImplTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/lineage/controller/KnowledgeLineageControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/lineage/controller/KnowledgePortalLineageControllerTest.java`
    - 处理动作：补齐后端世系画布读取和接口测试。
    - 验收点：测试覆盖字段映射、空态、权限、默认最新版本和只读边界。
    - 重要度：9/10

- [ ] `Frontend lineage tests`：14 补齐前端世系页面测试
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/lineage-page.test.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/lineage/components/lineage-canvas.test.tsx`、`kuzhambu-apps/portal-web/src/pages/knowledge/knowledge-lineage-page.test.tsx`、`kuzhambu-apps/admin-web/src/app.test.tsx`
    - 处理动作：补齐 Admin 和 Portal 世系前端测试。
    - 验收点：测试覆盖权限空态、筛选控件、画布选中、列表联动、Portal 只读行为和菜单可达性。
    - 重要度：9/10

- [ ] `Branch sync`：15 同步 main 分支代码
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`feat/knowledge-lineage-visualization`、`origin/main`
    - 处理动作：在收口验证前把当前分支同步到最新 `origin/main`。
    - 验收点：当前分支包含最新 `origin/main`，冲突已解决且无不相关文件改动。
    - 重要度：10/10

- [ ] `Final validation`：16 运行最终验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`、`.github/workflows/pr-verify.yml`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/admin-web/`、`kuzhambu-apps/portal-web/`
    - 处理动作：运行 RUNBOOK 指定的 Java、Admin Web 和 Portal Web 验证。
    - 验收点：格式、静态检查、后端测试、前端 lint/test/build 通过，失败项已修复或明确记录。
    - 重要度：10/10

- [ ] `Knowledge documentation coverage`：17 更新 Knowledge Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：同步世系图独立可视化闭环的稳定设计和覆盖状态。
    - 验收点：Implementation Coverage 将世系图浏览从部分完成更新为与实际交付一致，设计文档记录独立 Admin/Portal 入口。
    - 重要度：10/10

- [ ] `Task closeout`：18 清理 RUNBOOK 和 TODO
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`
    - 范围对象：`docs/30-designs/RUNBOOK-KNOWLEDGE-LINEAGE-VISUALIZATION.md`、`TODO.md`
    - 处理动作：删除已完成 RUNBOOK 并按完成情况删除或收窄 TODO。
    - 验收点：PR 收口前不保留已完成 RUNBOOK，`TODO.md` 不记录已完成任务历史。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
