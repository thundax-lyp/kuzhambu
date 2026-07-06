# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `system menu seed`：新增后台质量报告菜单和权限种子
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`db/data-source/system.json`、`db/data/system.sql`
    - 处理动作：新增 `质量报告` 菜单和 `knowledge:quality-report:view|generate` 权限种子，并重新生成系统数据 SQL。
    - 验收点：后台菜单可导航到 `/knowledge/quality-report`，权限点与后端 `@HasPermission` 一致。
    - 重要度：9/10

- [ ] `knowledge tests`：补齐质量报告后端测试
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/refinement/KnowledgeQualityReportApplicationServiceTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/admin/refinement/controller/KnowledgeQualityReportControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/test/java/com/thundax/kuzhambu/knowledge/interfaces/portal/quality/controller/KnowledgePortalQualityControllerTest.java`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-infra/src/test/java/com/thundax/kuzhambu/knowledge/infra/refinement/repository/impl/QualityReportRepositoryTest.java`
    - 处理动作：新增或更新报告生成、后台接口、Portal 空态和仓储读取测试。
    - 验收点：测试覆盖报告主表/问题/来源明细保存、最新 `PUBLISHED` 排序、Portal 空态和后台接口权限路径。
    - 重要度：10/10

- [ ] `quality loop validation`：运行 Knowledge 质量闭环验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`kuzhambu-servers/`、`kuzhambu-apps/`
    - 处理动作：运行 RUNBOOK 中 Java servers 与 admin-web 的格式化、静态检查和测试命令。
    - 验收点：`mvn spotless:check`、`mvn checkstyle:check`、Knowledge 相关 Maven test、`npm run format:check`、`npm run lint`、admin-web test 均通过，或记录明确阻塞。
    - 重要度：10/10

- [ ] `Knowledge documentation closure`：更新覆盖矩阵并清理 RUNBOOK
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`
    - 范围对象：`docs/30-designs/KNOWLEDGE-DESIGN.md`、`docs/40-readiness/KNOWLEDGE-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-QUALITY-LOOP.md`、`TODO.md`
    - 处理动作：将质量标注报告闭环写入 Knowledge 设计和 Implementation Coverage，删除已完成 RUNBOOK，并按完成情况清理或收窄 TODO。
    - 验收点：Coverage 不再把质量报告闭环标为未完成，RUNBOOK 文件已删除，`TODO.md` 不保留已完成任务。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
