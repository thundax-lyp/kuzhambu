# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `db/schema db/data`：归并 SQL 文件和表名前缀
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`db/schema/`、`db/data/`
    - 处理动作：按新业务域归并 SQL 文件，并将设计阶段表名前缀收敛为新业务域前缀
    - 验收点：新 SQL 文件覆盖旧 DDL 和初始化数据，旧 SQL 文件已删除或不再被引用
    - 重要度：10/10

- [ ] `kuzhambu-servers`：重建 Maven 模块结构
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/pom.xml`、`kuzhambu-servers/biz/`、`kuzhambu-servers/starter/`
    - 处理动作：将后端模块结构调整为 `common/biz/starter`，并在 `biz` 下建立七个业务域四层模块
    - 验收点：Maven reactor 使用新模块结构，旧横向 `infra` 和 `interfaces` 模块不再作为目标入口
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/system`：迁移系统基础域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-core/`、`kuzhambu-servers/biz/kuzhambu-biz-auth/`、`kuzhambu-servers/biz/kuzhambu-biz-audit/`、`kuzhambu-servers/infra/kuzhambu-infra-core/`、`kuzhambu-servers/infra/kuzhambu-infra-auth/`、`kuzhambu-servers/infra/kuzhambu-infra-audit/`、`kuzhambu-servers/biz/system/`
    - 处理动作：将 core、auth、audit 迁移到 system 域四层模块并更新 package/import
    - 验收点：system 域代码位于 `com.thundax.kuzhambu.system` 包下，用户权限、认证会话和业务审计边界清晰
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/storage`：迁移文件存储域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-storage/`、`kuzhambu-servers/infra/kuzhambu-infra-storage/`、`kuzhambu-servers/biz/storage/`
    - 处理动作：将 storage 迁移到 storage 域四层模块并更新 package/import
    - 验收点：storage 域代码位于 `com.thundax.kuzhambu.storage` 包下，文件对象、引用、读取和上传能力保持完整
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/classics`：迁移古籍域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-sancai/`、`kuzhambu-servers/biz/kuzhambu-biz-wangqi/`、`kuzhambu-servers/biz/kuzhambu-biz-mingcustoms/`、`kuzhambu-servers/biz/kuzhambu-biz-sharing/`、`kuzhambu-servers/infra/kuzhambu-infra-sancai/`、`kuzhambu-servers/infra/kuzhambu-infra-wangqi/`、`kuzhambu-servers/infra/kuzhambu-infra-mingcustoms/`、`kuzhambu-servers/infra/kuzhambu-infra-sharing/`、`kuzhambu-servers/biz/classics/`
    - 处理动作：将三才图会、王圻文档、明代习俗和分享迁移到 classics 域四层模块并更新 package/import
    - 验收点：classics 域代码位于 `com.thundax.kuzhambu.classics` 包下，三类古籍内容和分享子能力边界清晰
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/ai`：迁移 AI 生产域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-ai-config/`、`kuzhambu-servers/biz/kuzhambu-biz-ai-refinement/`、`kuzhambu-servers/infra/kuzhambu-infra-ai-config/`、`kuzhambu-servers/infra/kuzhambu-infra-ai-refinement/`、`kuzhambu-servers/biz/ai/`
    - 处理动作：将 AI 配置、提示词和 AI 精修迁移到 ai 域四层模块并更新 package/import
    - 验收点：ai 域代码位于 `com.thundax.kuzhambu.ai` 包下，模型配置、提示词和候选确认链路保持完整
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/knowledge`：迁移知识组织域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-taxonomy/`、`kuzhambu-servers/biz/kuzhambu-biz-data-refinement/`、`kuzhambu-servers/biz/kuzhambu-biz-knowledge-graph/`、`kuzhambu-servers/infra/kuzhambu-infra-taxonomy/`、`kuzhambu-servers/infra/kuzhambu-infra-data-refinement/`、`kuzhambu-servers/infra/kuzhambu-infra-knowledge-graph/`、`kuzhambu-servers/biz/knowledge/`
    - 处理动作：将标签治理、数据精修和知识图谱迁移到 knowledge 域四层模块并更新 package/import
    - 验收点：knowledge 域代码位于 `com.thundax.kuzhambu.knowledge` 包下，标签、同义词、实体关系和图谱质量能力保持完整
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/discovery`：迁移知识发现域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-search/`、`kuzhambu-servers/biz/kuzhambu-biz-qa/`、`kuzhambu-servers/infra/kuzhambu-infra-search/`、`kuzhambu-servers/infra/kuzhambu-infra-qa/`、`kuzhambu-servers/biz/discovery/`
    - 处理动作：将搜索和智能问答迁移到 discovery 域四层模块并更新 package/import
    - 验收点：discovery 域代码位于 `com.thundax.kuzhambu.discovery` 包下，搜索、问答、来源引用和日志能力保持完整
    - 重要度：10/10

- [ ] `kuzhambu-servers/biz/operations`：迁移运营运维域代码和接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/biz/kuzhambu-biz-operations/`、`kuzhambu-servers/infra/kuzhambu-infra-operations/`、`kuzhambu-servers/biz/operations/`
    - 处理动作：将 operations 迁移到独立运营运维域四层模块并更新 package/import
    - 验收点：operations 域代码位于 `com.thundax.kuzhambu.operations` 包下，不属于 system 或 starter，且只保存本领域自有规则和表
    - 重要度：9/10

- [ ] `kuzhambu-servers/starter`：创建后台和前台启动应用
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/`、`kuzhambu-servers/starter/kuzhambu-portal-starter/`
    - 处理动作：将旧 admin/portal 启动入口调整为 starter，并限制各自扫描 admin 或 portal 接口包
    - 验收点：admin starter 不暴露 portal Controller，portal starter 不暴露 admin Controller
    - 重要度：9/10

- [ ] `scripts verify`：更新验证入口并完成迁移验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`scripts/verify-all.sh`、`docs/40-readiness/PR-WORKFLOW.md`、`kuzhambu-servers/`
    - 处理动作：更新验证入口并运行 Maven reactor、架构规则和统一验证脚本
    - 验收点：相关验证命令通过，PR 工作流记录新的验证口径
    - 重要度：10/10

- [ ] `repo cleanup`：清理旧模块、旧文档和 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-SERVERS-DOMAIN-MODULE-MERGE.md`
    - 范围对象：`kuzhambu-servers/`、`docs/10-requirements/`、`docs/30-designs/`、`db/`、`TODO.md`
    - 处理动作：删除旧横向模块、旧需求设计文档、旧 SQL 文件，并在 PR 收口前删除本 RUNBOOK
    - 验收点：旧入口不再被引用，已完成 TODO 已删除或收窄
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
