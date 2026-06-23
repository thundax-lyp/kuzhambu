# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
