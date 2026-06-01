# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `AI V3`：验证 AI 持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#v3-ai-persistence-verification`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/`
    - 处理动作：按 RUNBOOK V3 的 3 个关联文件验证 AI SQL、DO、Mapper 和 Repository 最小读写。
    - 验收点：`db/schema/ai.sql` 和 `db/data/ai.sql` 可加载，核心表最小 CRUD 通过。
    - 重要度：8/10

- [ ] `AI V4`：执行 AI 模块验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#v4-module-verification`
    - 范围对象：`kuzhambu-servers/biz/ai/pom.xml`、`kuzhambu-servers/biz/ai/kuzhambu-ai-domain/pom.xml`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/pom.xml`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/pom.xml`、`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/pom.xml`
    - 处理动作：按 RUNBOOK V4 的 5 个关联文件执行 AI 模块最小格式、静态检查和编译验证。
    - 验收点：从 `kuzhambu-servers/` 执行 `mvn spotless:apply`、`mvn checkstyle:check` 和 AI 模块相关测试通过。
    - 重要度：10/10

- [ ] `AI CLOSE`：清理 AI 实现现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/30-designs/RUNBOOK-AI-DOMAIN.md`
    - 范围对象：`TODO.md`、`docs/30-designs/RUNBOOK-AI-DOMAIN.md`
    - 处理动作：AI 域实现和验证完成后删除临时 RUNBOOK，并清空或收窄 AI 相关 TODO。
    - 验收点：无剩余临时 RUNBOOK 引用，`TODO.md` 不保留已完成任务清单。
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
