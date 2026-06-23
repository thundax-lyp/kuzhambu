# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `knowledge/taxonomy`：落地标签合并内容引用复制
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/**`
    - 处理动作：将源标签内容引用补齐复制到目标标签，同时保留源标签历史引用
    - 验收点：目标标签获得缺失内容引用，源标签历史引用未被物理删除
    - 重要度：8/10

- [ ] `knowledge/taxonomy`：收敛合并后标签解析语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/test/java/com/thundax/kuzhambu/knowledge/domain/service/**`
    - 处理动作：让标签名和别名解析在命中已并入标签时自动回落到目标标签
    - 验收点：合并后源标签名和既有别名仍能解析到目标标签，不进入新的可用集合
    - 重要度：8/10

- [ ] `knowledge/taxonomy`：落地标签废弃动作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/**`
    - 处理动作：补齐标签废弃动作及最小字段落点，不做物理删除
    - 验收点：管理员可废弃标签，历史引用保留，废弃标签退出新的可用集合
    - 重要度：8/10

- [ ] `knowledge/taxonomy`：定义标签治理完整统计读契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/**`
    - 处理动作：为使用排行、知识库分布、来源占比、月度新增趋势补齐统计读契约
    - 验收点：后端统计读取输入输出已稳定，月度趋势按首次进入正式可用集合时间统计
    - 重要度：7/10

- [ ] `knowledge/taxonomy`：落地标签治理完整统计读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/**`、`db/schema/knowledge.sql`
    - 处理动作：以聚合读模型落地完整治理统计，必要时补最小时间维度索引
    - 验收点：后端可稳定返回完整治理统计，无需新增统计快照表
    - 重要度：7/10

- [ ] `knowledge/taxonomy`：暴露合并预览、合并、废弃、统计后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/**`
    - 处理动作：补齐 taxonomy 后台业务动作接口并统一命名语义
    - 验收点：后台已提供 merge preview、merge apply、deprecate、metrics 的稳定接口
    - 重要度：8/10

- [ ] `admin-web/taxonomy`：补齐标签合并预览与合并动作
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/**`
    - 处理动作：在现有 taxonomy 页面补齐合并预览和合并动作入口
    - 验收点：管理员可在 taxonomy 页面完成合并预览和合并，不混入统计视图
    - 重要度：8/10

- [ ] `admin-web/taxonomy`：补齐标签废弃与完整统计入口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/taxonomy/**`
    - 处理动作：在现有 taxonomy 页面补齐废弃动作和完整治理统计展示入口
    - 验收点：管理员可执行废弃动作并查看使用排行、知识库分布、来源占比和月度新增趋势
    - 重要度：8/10

- [ ] `knowledge/graph`：定义图谱版本读取契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/**`
    - 处理动作：为图谱版本列表与详情补齐 application 读契约、结果模型和测试
    - 验收点：后端已稳定表达图谱版本读取输入输出
    - 重要度：8/10

- [ ] `knowledge/graph`：落地图谱版本读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/**`
    - 处理动作：基于 `knowledge_graph_version` 落地版本列表与详情读取
    - 验收点：后端可返回版本列表和版本详情，作为正式结果页默认主入口
    - 重要度：8/10

- [ ] `knowledge/graph`：定义正式实体读取契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/**`
    - 处理动作：为正式实体列表与详情补齐 application 读契约、结果模型和测试
    - 验收点：后端已稳定表达正式实体读取输入输出
    - 重要度：8/10

- [ ] `knowledge/graph`：落地正式实体读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/**`
    - 处理动作：基于 `knowledge_entity` 落地正式实体列表与详情读取
    - 验收点：后端可稳定返回实体名称、类型、确认状态、最新版本关联和来源引用
    - 重要度：8/10

- [ ] `knowledge/graph`：定义正式关系读取契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/**`
    - 处理动作：为正式关系列表与详情补齐 application 读契约、结果模型和测试
    - 验收点：后端已稳定表达正式关系读取输入输出
    - 重要度：8/10

- [ ] `knowledge/graph`：落地正式关系读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/**`
    - 处理动作：基于 `knowledge_relation` 落地正式关系列表与详情读取
    - 验收点：后端可稳定返回源、目标、关系类型、确认状态、最新版本关联和来源引用
    - 重要度：8/10

- [ ] `knowledge/graph`：定义正式世系读取契约
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/graph/**`
    - 处理动作：为正式世系列表与详情补齐 application 读契约、结果模型和测试
    - 验收点：后端已稳定表达正式世系结果读取输入输出
    - 重要度：8/10

- [ ] `knowledge/graph`：落地正式世系读取
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/graph/**`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/graph/**`
    - 处理动作：基于 `knowledge_lineage_node` 和 `knowledge_lineage_relation` 落地正式世系列表与详情读取
    - 验收点：后端可稳定返回世系节点和关系的确认状态、版本关联和来源引用
    - 重要度：8/10

- [ ] `knowledge/graph`：暴露图谱版本与正式结果后台接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java/com/thundax/kuzhambu/knowledge/interfaces/admin/graph/**`
    - 处理动作：补齐图谱版本、正式实体、正式关系、正式世系读取接口
    - 验收点：后台已提供以图谱版本列表为主入口的正式结果读取接口
    - 重要度：8/10

- [ ] `admin-web/knowledge`：搭建正式结果读取页骨架和路由
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/**`、`kuzhambu-apps/admin-web/src/router/index.tsx`
    - 处理动作：新增正式结果读取页组骨架和路由入口
    - 验收点：Admin Web 已有独立于 taxonomy 的正式结果读取页入口
    - 重要度：7/10

- [ ] `admin-web/knowledge`：接通图谱版本列表和详情
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/**`
    - 处理动作：接通图谱版本读取 service contract、列表和详情交互
    - 验收点：管理员可从图谱版本列表进入版本详情
    - 重要度：7/10

- [ ] `admin-web/knowledge`：接通正式实体、关系、世系列表与详情
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`kuzhambu-apps/admin-web/src/pages/knowledge/**`
    - 处理动作：基于版本入口接通正式实体、关系和世系列表与详情
    - 验收点：管理员可从版本详情下钻查看正式实体、关系和世系结果
    - 重要度：7/10

- [ ] `docs/knowledge`：同步覆盖状态并收口现场
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-GOVERNANCE-READABLE-RESULTS.md`
    - 范围对象：`docs/30-designs/*`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`TODO.md`
    - 处理动作：在功能落地后同步设计与 coverage 文档，并清理 RUNBOOK 与 TODO
    - 验收点：文档只反映已落地结果，PR 前 RUNBOOK 删除且 TODO 收空或收窄
    - 重要度：6/10

## 待审阅任务项

## 待讨论项
