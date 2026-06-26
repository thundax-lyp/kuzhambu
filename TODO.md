# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/report/`
    - 处理动作：提供 AI 调用量、失败量、平均延迟和总成本的 summary 只读入口
    - 验收点：Operations 可一次读取 AI 报表所需统计
    - 重要度：8/10

- [ ] `kuzhambu-discovery-application/report`：新增 Discovery 报表统计读接口
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/discovery/kuzhambu-discovery-application/src/main/java/com/thundax/kuzhambu/discovery/application/report/`
    - 处理动作：提供搜索量、问答量、趋势和热门查询的 summary 只读入口
    - 验收点：Operations 不再直接查询 Discovery 日志明细
    - 重要度：8/10

- [ ] `kuzhambu-knowledge-application/report`：新增 Knowledge 报表统计读接口
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-application/src/main/java/com/thundax/kuzhambu/knowledge/application/report/`
    - 处理动作：提供标签覆盖率、标签排行、分类分布和新增趋势的 summary 只读入口
    - 验收点：Operations 可按统一规格读取 Knowledge 报表统计
    - 重要度：8/10

- [ ] `operations metrics gateway`：建立 Operations 聚合网关
    - 任务类型：拆解任务
    - 依据文档：`docs/30-designs/RUNBOOK-OPERATIONS-REPORT-CLOSURE.md`
    - 范围对象：`kuzhambu-servers/biz/operations/kuzhambu-operations-application/src/main/java/com/thundax/kuzhambu/operations/application/report/support/`
    - 处理动作：在 Operations 内聚合 Classics、AI、Discovery、Knowledge 的 summary 读接口
    - 验收点：Operations 只依赖统一 metrics gateway，不直接耦合多域 service 细节
    - 重要度：9/10

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
