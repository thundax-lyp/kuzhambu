# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `classics/tag-app-manual`：接通手工标签到 Knowledge 的同步闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsTagBindingSupport.java`
    - 处理动作：在 Classics 应用层引入统一标签绑定支撑，确保手工标签新增、更新、删除都先解析/创建统一标签再同步 Knowledge 引用
    - 验收点：手工标签流程不再只写本地表，应用层通过统一支撑完成 Classics 主事实与 Knowledge 引用同步
    - 重要度：10/10

- [ ] `classics/tag-repo-scope`：修复 Classics 标签仓储排序和查询作用域
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-domain/src/main/java/com/thundax/kuzhambu/classics/domain/content/repository/ClassicsContentRepository.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/command/ContentTagSortCommand.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/content/repository/impl/ClassicsContentRepositoryImpl.java`
    - 处理动作：移除全表标签排序依赖，改为按 `contentType + contentId` 作用域查询、排序和删除标签
    - 验收点：仓储与排序命令均显式带内容作用域，不再存在全表标签排序输入
    - 重要度：10/10

- [ ] `classics/ai-tag-closure`：接通 AI 标签确认到 Knowledge 的同步闭环
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/controller/ClassicsContentAdminController.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/main/java/com/thundax/kuzhambu/classics/interfaces/admin/content/assembler/ClassicsContentInterfaceAssembler.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/service/impl/ClassicsContentApplicationServiceImpl.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/main/java/com/thundax/kuzhambu/classics/application/content/support/ClassicsAiCandidatePayloadParser.java`
    - 处理动作：将 `capability=tags` 的 AI 候选确认逻辑改为先走 Knowledge 协作语义，再回写 Classics 与 Knowledge
    - 验收点：AI 标签确认路径不再直接按名称重建本地标签，统一标签解析与引用同步完整生效
    - 重要度：10/10

- [ ] `knowledge/tag-binding-service`：新增 Knowledge 标签绑定协作语义
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/KnowledgeTagBindingDomainService.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/service/impl/KnowledgeTagBindingDomainServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagAliasRepository.java`
    - 处理动作：在 Knowledge 领域层新增统一标签解析、自动创建和内容引用同步的稳定协作语义
    - 验收点：Knowledge 领域层存在可被 Classics 编排调用的绑定协作服务，不依赖后台 taxonomy CRUD
    - 重要度：10/10

- [ ] `knowledge/tag-content-ref-repo`：扩展 Knowledge 内容引用仓储能力
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-domain/src/main/java/com/thundax/kuzhambu/knowledge/domain/taxonomy/repository/TagContentRefRepository.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagContentRefRepositoryImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/persistence/assembler/TaxonomyPersistenceAssembler.java`
    - 处理动作：为内容引用增加按内容查询、按内容删除、按内容重建所需的仓储方法和装配逻辑
    - 验收点：Knowledge 内容引用仓储可支持单内容同步、删除和重建，不需要越层访问底表
    - 重要度：9/10

- [ ] `knowledge/taxonomy-compat`：收敛 Knowledge taxonomy 后台服务与引用兼容口径
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/taxonomy/service/impl/TaxonomyApplicationServiceImpl.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/main/java/com/thundax/kuzhambu/knowledge/infra/taxonomy/repository/impl/TagRepositoryImpl.java`
    - 处理动作：在保持后台治理用例不扩张的前提下，同步调整 taxonomy 服务和标签仓储对新枚举/来源口径的兼容
    - 验收点：Knowledge 后台 taxonomy 页面语义不变，且与新的协作枚举和绑定服务不冲突
    - 重要度：7/10

- [ ] `tests/classics-tag-flow`：补 Classics 标签闭环测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceImplTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/content/ClassicsContentApplicationServiceAiCandidateTest.java`、`kuzhambu-servers/biz/classics/kuzhambu-classics-interface/src/test/java/com/thundax/kuzhambu/classics/interfaces/admin/content/ClassicsContentAdminControllerTest.java`
    - 处理动作：为手工标签、AI 标签确认、删除标签、按内容排序补齐 Classics 应用和接口测试
    - 验收点：Classics 标签闭环核心路径均有测试覆盖，旧的全表排序和本地直写行为不再通过测试
    - 重要度：9/10

- [ ] `tests/knowledge-binding-flow`：补 Knowledge 协作与兼容测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/taxonomy/KnowledgeTagBindingDomainServiceTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/taxonomy/KnowledgeTaxonomyCompatibilityTest.java`
    - 处理动作：补统一标签解析、自动创建、内容引用同步/删除/重建，以及 taxonomy 后台兼容性测试
    - 验收点：Knowledge 绑定协作服务与 taxonomy 后台兼容行为均可自动验证
    - 重要度：8/10

- [ ] `docs/closure-sync`：同步闭环设计与覆盖文档
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`
    - 范围对象：`docs/30-designs/CLASSICS-DESIGN.md`、`docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/40-readiness/CLASSICS-IMPLEMENTATION-COVERAGE.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`
    - 处理动作：将闭环后的主事实、派生模型、接口口径和覆盖状态同步回正式设计与覆盖清单
    - 验收点：正式设计与覆盖文档不再保留与闭环实现相冲突的旧口径
    - 重要度：7/10

- [ ] `cleanup/todo-runbook-scene`：收口并清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-CLASSICS-KNOWLEDGE-CLOSURE.md`、相关临时测试/脚本/重建辅助文件
    - 处理动作：在任务真正完成后删除已完成 TODO、清理无剩余价值的 RUNBOOK 和临时现场文件
    - 验收点：`TODO.md` 仅保留剩余未完成项，RUNBOOK 和临时现场符合收口规则且无过期残留
    - 重要度：8/10

## 待讨论项
