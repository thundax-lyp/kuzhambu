# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Cross-domain seed data`：补最小联调 seed
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-KNOWLEDGE-CENTRIC-LOOP.md`
    - 范围对象：`db/data/classics.sql`、`db/data/knowledge.sql`、`db/data/discovery.sql`、`db/data/ai.sql`
    - 处理动作：补齐跨域闭环所需最小内容、taxonomy、Discovery 与 AI 种子数据
    - 验收点：本地库能支撑 search 与 qa 联调
    - 重要度：8/10

- [ ] `Backend verify`：按模块补 Java 验证并收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：本轮新增或修改的 Java 测试文件与对应模块
    - 处理动作：分批运行 spotless、checkstyle 和相关模块测试
    - 验收点：相关 Java 模块验证通过且失败时有明确修复记录
    - 重要度：8/10

- [ ] `Frontend verify`：按页面补前端验证并收口
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：本轮新增或修改的前端测试文件与对应 workspace
    - 处理动作：分批运行 format、lint、test 和 build
    - 验收点：相关前端 workspace 验证通过且失败时有明确修复记录
    - 重要度：8/10

- [ ] `Docs and cleanup`：完成 coverage 文档同步与清理现场
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`
    - 范围对象：`docs/40-readiness/{AI,DISCOVERY,KNOWLEDGE,CLASSICS}-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-KNOWLEDGE-CENTRIC-LOOP.md`、临时脚本与临时数据
    - 处理动作：同步覆盖文档并清理无保留价值的 RUNBOOK 与临时产物
    - 验收点：文档口径与最终代码一致，工作区不残留无用临时文件
    - 重要度：8/10

## 待审阅任务项

## 待讨论项
