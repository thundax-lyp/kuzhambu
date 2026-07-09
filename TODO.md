# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `DISCOVERY-QUALITY-001`：确认 Discovery 搜索运行质量 RUNBOOK 与固定数据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`
    - 范围对象：`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`
    - 处理动作：审核并锁定预发环境、固定测试数据、provider 预检、ES green 门槛和证据归档口径
    - 验收点：RUNBOOK 可直接执行，且不包含中间状态、Portal 登录依赖或本地环境作为最终证据来源
    - 重要度：9/10

- [ ] `DISCOVERY-QUALITY-002`：对齐 Discovery Search 点击日志 schema
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`
    - 范围对象：`db/schema/discovery.sql`、`db/data/discovery.sql`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/dataobject/SearchClickDO.java`、`kuzhambu-servers/biz/discovery/kuzhambu-discovery-infra/src/main/java/com/thundax/kuzhambu/discovery/infra/search/persistence/mapper/SearchClickMapper.java`
    - 处理动作：确认运行态 `discovery_search_click` 表与 schema seed 的字段、索引和示例数据一致
    - 验收点：schema、seed、DO 和 mapper 使用同一张点击日志表，Search click 冒烟不会因表名或字段不一致阻断
    - 重要度：9/10

- [ ] `DISCOVERY-QUALITY-003`：执行 Discovery Search 质量冒烟并归档证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`
    - 范围对象：`discovery-search` 索引、`/api/discovery/search-admin/*`、`/api/portal/discovery/search/*`、`discovery_search_log`、`discovery_search_click`
    - 处理动作：按 RUNBOOK 验证 ES health、索引重建、Portal 检索高亮、结果深链、点击日志和 Admin 搜索分析
    - 验收点：证据包包含 ES、rebuild、search response、click、Admin log、DB 查询和页面截图，所有 Search stop condition 均未触发
    - 重要度：10/10

- [ ] `DISCOVERY-QUALITY-004`：执行 Discovery QA 来源跳转冒烟并归档证据
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`
    - 范围对象：`/api/discovery/qa-admin/*`、`/api/portal/discovery/qa/*`、`discovery_qa_session`、`discovery_qa_message_source`、`discovery_qa_retrieval_trace`
    - 处理动作：按 RUNBOOK 验证 provider 预检、QA health、QA rebuild、Portal QA 带来源回答、来源跳转和 Admin trace
    - 验收点：证据包包含 QA health、rebuild、chat response、Admin session/source/trace、DB 查询和页面截图，所有 QA stop condition 均未触发
    - 重要度：10/10

- [ ] `DISCOVERY-QUALITY-005`：收口 Discovery 质量证据并同步 main
    - 任务类型：执行任务
    - 依据文档：`docs/00-governance/TODO-RULES.md`、`docs/00-governance/PR-RULES.md`、`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`
    - 范围对象：`docs/40-readiness/DISCOVERY-IMPLEMENTATION-COVERAGE.md`、`docs/30-designs/RUNBOOK-DISCOVERY-QUALITY-SMOKE.md`、`TODO.md`、当前工作分支
    - 处理动作：更新 Discovery Implementation Coverage，清理已完成 RUNBOOK 和对应 TODO，并在收口前同步 `main` 分支最新代码
    - 验收点：coverage 记录最终证据链接和剩余风险，RUNBOOK 已按规则清理，TODO 删除或收窄已完成项，当前分支已基于最新 `main` 复核
    - 重要度：10/10

## 待审阅任务项

## 待讨论项
