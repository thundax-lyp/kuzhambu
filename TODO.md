# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `operations interface tests`：补齐报表接口契约测试
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-interface/src/test/java/com/thundax/kuzhambu/operations/interfaces/admin/report/controller/`
    - 处理动作：锁定 `generate`、`page`、`detail` 的路由、权限和响应模型契约
    - 验收点：Operations 报表接口 contract 可回归
    - 重要度：7/10

- [ ] `operations app/infra tests`：补齐报表应用与基础设施测试
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/test/java/com/thundax/kuzhambu/operations/application/report/`、`kuzhambu-servers/biz/operations/kuzhambu-operations-infra/src/test/java/com/thundax/kuzhambu/operations/infra/report/`
    - 处理动作：锁定发起任务、状态流转、worker 协议解析和 report repository 行为
    - 验收点：成功回写、失败回写和查询行为有最小自动化保障
    - 重要度：7/10

- [ ] `关联域 report tests`：补齐关联域 summary 统计测试
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/classics/kuzhambu-classics-application/src/test/java/com/thundax/kuzhambu/classics/application/report/`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/report/`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/test/java/com/thundax/kuzhambu/discovery/application/report/`、`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/test/java/com/thundax/kuzhambu/knowledge/application/report/`
    - 处理动作：固定 Classics、AI、Discovery、Knowledge 的 summary 统计口径
    - 验收点：四个关联域的 summary 统计字段与趋势 bucket 被测试锁定
    - 重要度：7/10

## 待讨论项
