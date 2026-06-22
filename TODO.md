# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项





- [ ] `knowledge taxonomy requests A`：创建分类请求模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryPageRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryCreateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryUpdateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCategoryStatusRequest.java`
    - 处理动作：创建标签分类接口请求模型。
    - 验收点：4 个请求类存在且字段能覆盖分类分页、创建、更新、状态修改。
    - 重要度：8/10

- [ ] `knowledge taxonomy requests B`：创建标签请求模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagPageRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagCreateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagUpdateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagStatusRequest.java`
    - 处理动作：创建标签分页、创建、更新、状态请求模型。
    - 验收点：4 个请求类存在且字段能覆盖标签管理主流程。
    - 重要度：9/10

- [ ] `knowledge taxonomy requests C`：创建标签详情与审核请求模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagDetailRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagReviewPageRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagReviewRequest.java`
    - 处理动作：创建标签详情与审核请求模型。
    - 验收点：3 个请求类存在且字段能覆盖详情读取与审核流程。
    - 重要度：8/10

- [ ] `knowledge taxonomy requests D`：创建别名与同义词请求模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagAliasCreateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/TagAliasRemoveRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymPageRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymCreateRequest.java`
    - 处理动作：创建别名与同义词部分请求模型。
    - 验收点：4 个请求类存在且字段能覆盖别名新增删除与同义词分页创建。
    - 重要度：8/10

- [ ] `knowledge taxonomy requests E`：补齐同义词更新删除请求模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymUpdateRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymStatusRequest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/request/SynonymRemoveRequest.java`
    - 处理动作：创建同义词更新、状态、删除请求模型。
    - 验收点：3 个请求类存在且字段能覆盖同义词更新、启用禁用、删除。
    - 重要度：7/10

- [ ] `knowledge taxonomy responses A`：创建分类与标签响应模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagCategoryResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagDetailResponse.java`
    - 处理动作：创建标签分类、标签列表、标签详情响应模型。
    - 验收点：3 个响应类存在且字段与 RUNBOOK 响应契约一致。
    - 重要度：8/10

- [ ] `knowledge taxonomy responses B`：创建别名内容引用与同义词响应模型
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagAliasResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/TagContentRefResponse.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/response/SynonymResponse.java`
    - 处理动作：创建别名、内容引用、同义词响应模型。
    - 验收点：3 个响应类存在且字段与 RUNBOOK 响应契约一致。
    - 重要度：7/10

- [ ] `knowledge taxonomy controller`：创建 taxonomy 控制器与接口装配器
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/controller/KnowledgeTaxonomyController.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/assembler/KnowledgeTaxonomyInterfaceAssembler.java`
    - 处理动作：创建 taxonomy Admin 控制器与接口装配器并挂接固定 API 与权限码。
    - 验收点：控制器存在且 POST 路径、权限码、方法名与 RUNBOOK 完全一致。
    - 重要度：10/10

- [ ] `knowledge taxonomy frontend types and service`：创建 taxonomy 前端类型与服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-types.ts`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-service.ts`
    - 处理动作：创建 taxonomy 前端类型定义与 API 服务封装。
    - 验收点：类型文件覆盖 RUNBOOK 固定类型名且服务文件覆盖 RUNBOOK 固定方法名。
    - 重要度：9/10

- [ ] `knowledge taxonomy route`：接入 taxonomy 路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：注册 `/knowledge/taxonomy` 路由并指向 `TaxonomyPage`。
    - 验收点：路由文件存在 taxonomy 页面 import 与 `/knowledge/taxonomy` 路由项。
    - 重要度：9/10

- [ ] `knowledge taxonomy page shell`：创建 taxonomy 页面壳与样式
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/taxonomy-page.css`
    - 处理动作：创建 taxonomy 主页面与样式并搭起 4 个固定 Tabs 布局。
    - 验收点：主页面存在且渲染 `标签分类`、`统一标签`、`待审核标签`、`同义词` 四个页签。
    - 重要度：10/10

- [ ] `knowledge taxonomy category components`：创建分类表格与编辑组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/category-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/category-edit.tsx`
    - 处理动作：创建标签分类表格与编辑组件。
    - 验收点：2 个组件存在且能支撑分类列表、新增、编辑、启用、禁用。
    - 重要度：8/10

- [ ] `knowledge taxonomy tag components A`：创建标签表格与编辑组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-edit.tsx`
    - 处理动作：创建统一标签表格与编辑组件。
    - 验收点：2 个组件存在且能支撑标签列表、新增、编辑、启用、禁用。
    - 重要度：9/10

- [ ] `knowledge taxonomy tag components B`：创建标签审核与详情组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-review-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-detail-drawer.tsx`
    - 处理动作：创建待审核标签表格与标签详情抽屉组件。
    - 验收点：2 个组件存在且能支撑标签审核、详情查看、内容引用只读展示。
    - 重要度：9/10

- [ ] `knowledge taxonomy tag alias component`：创建标签别名列表组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/tag-alias-list.tsx`
    - 处理动作：创建标签别名列表组件。
    - 验收点：组件存在且能支撑标签详情中的别名列表、新增、删除。
    - 重要度：7/10

- [ ] `knowledge taxonomy synonym components`：创建同义词表格与编辑组件
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/synonym-table.tsx`、`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/components/synonym-edit.tsx`
    - 处理动作：创建同义词表格与编辑组件。
    - 验收点：2 个组件存在且能支撑同义词分页、新增、编辑、启用、禁用、删除。
    - 重要度：8/10

- [ ] `knowledge menu seed source`：补充知识治理菜单 JSON 源
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`db/data-source/system.json`
    - 处理动作：按现有菜单结构新增 `知识治理` 一级菜单与 `标签与同义词` 二级菜单。
    - 验收点：`system.json` 中存在 `/knowledge` 与 `/knowledge/taxonomy` 菜单项且权限码、图标、优先级字段完整。
    - 重要度：9/10

- [ ] `knowledge menu seed sql`：根据 JSON 生成菜单 SQL
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`
    - 处理动作：基于更新后的菜单 JSON 重新生成 system 初始化 SQL。
    - 验收点：`db/data/system.sql` 包含知识治理菜单数据且与 `db/data-source/system.json` 一致。
    - 重要度：9/10

- [ ] `knowledge implementation coverage`：补充 Knowledge Implementation Coverage
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 范围对象：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：新增 Knowledge Implementation Coverage 文档并记录本次 MVP 的需求覆盖矩阵与未覆盖范围。
    - 验收点：`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md` 存在且明确标记本次已覆盖、未覆盖、超出范围项。
    - 重要度：8/10

- [ ] `knowledge mvp closeout`：清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`、`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-MVP.md`
    - 处理动作：在全部实现与覆盖文档完成后删除已完成 TODO 项并清理不再有价值的 RUNBOOK。
    - 验收点：`TODO.md` 仅保留剩余未完成任务且 `RUNBOOK-KNOWLEDGE-MVP.md` 在任务关闭时被删除或收窄为仍有残余价值的内容。
    - 重要度：10/10

## 待讨论项
