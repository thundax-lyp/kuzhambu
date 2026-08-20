# Coverage and Output

## Changed-file ledger

以 `collect-review-context.mjs context` 返回的完整 changed-file 集为唯一计数基线。rename 计一条。每个文件归入脚本给出的模块，并取一个终态：

- `reviewed`：结合 diff、必要上下文、调用点和测试完成行为审查。
- `mechanical`：读完 diff 后确认是纯格式、生成物或无独立行为的机械变化；记录依据。
- `not-applicable`：读完 diff 后确认不影响任何已识别系统承诺；记录原因。
- `deferred`：缺少必要上下文或未完成审查；记录具体缺口。

`mechanical` 和 `not-applicable` 不是免审标签。测试、seed、配置、迁移、脚本和文档同样计入。模块沿用脚本的稳定分类：

- `repo-governance`
- `docs`
- `db-seed`
- `deploy`
- `servers:<owning-module>`
- `apps:<app>` 或 `apps:workspace`
- `workers`
- `other`

按模块汇总 `total`、`reviewed`、`mechanical`、`not-applicable`、`deferred`。模块内各终态之和必须等于模块 total，所有模块 total 之和必须等于 changed-file 总数。逐文件列出 deferred、other 和 finding/contract surface 所需的证据文件。

## Contract-surface ledger

每条 surface 记录：

- 对应的系统承诺；
- 主风险模型或强制专项检查；
- changed-file anchor；
- producer/adapter 范围；
- 首个真实 validator；
- 最终 consumer/sink 或明确终点；
- fallback、历史数据、持久化、迁移、等价路径、测试或文档中的相关负空间；
- `reviewed`、`not-applicable` 或 `deferred` 终态及理由。

必须满足：

- 每条系统承诺至少映射一个 surface；
- 每个主风险模型至少映射一个 surface；
- 每个被触发的强制专项检查至少映射一个 surface；
- 找不到 validator、consumer/sink 或明确终点时必须 deferred。

任何缺失条目、数量不守恒、缺失映射或 deferred 都表示 coverage incomplete。只有两个 ledger 闭合且无 deferred 时，才能输出 `No actionable findings.`。

## Findings

Findings 放在最前，按 P0、P1、P2、P3 排序。每条使用：

```md
### [P0/P1/P2/P3] 简短、可执行的标题

* 文件：`path/to/file`
* 行号：最小 changed range
* 问题：具体错误及根因。
* 触发：能够从代码证明的场景或调用路径。
* 影响：用户、数据、权限、性能或维护后果。
* 建议：具体修复方向。
```

优先级：

- P0：严重安全事故、数据损坏、系统不可用或不可逆影响。
- P1：高概率造成核心流程错误、严重回归、权限失控或发布失败。
- P2：真实、可复现但影响范围有限的功能、兼容或运行问题。
- P3：低风险但作者仍会修复的维护性、结构性或长期演进问题；不包含格式和个人偏好。

位置优先指向 changed line；如果根因在新增调用方，指向调用方。依赖推断时明确推断依据。相同根因合并，不确定且无法证明的问题不写成 finding。

## Open questions

只列出需要作者确认且会影响理解、但不阻断已识别 surface 关闭的问题。凡是会影响 finding 是否成立或关键链路是否闭合的问题，必须把对应 surface 标为 deferred，而不能只放进 Open questions。

## 最终格式

Findings 或无 finding 声明之后，输出：

```md
## Coverage ledger

### Changed files

* `<module>` — total: N; reviewed: N; mechanical: N; not-applicable: N; deferred: N
  * mechanical: N — <reason>
  * not-applicable: N — <reason>
* Total — diff files: N; ledger files: N

Exceptions:

* `deferred` — `path/to/file`: <reason>

### Contract surfaces

* `reviewed` — <commitment / risk / special-check> — <surface>: anchor `<file>`; validator `<location>`; consumer/sink `<location>`
* `deferred` — <mapping> — <surface>: <missing evidence or endpoint>

### Validation gaps

* <None, or every deferred/missing item and material unrun validation.>

## Review summary

* 本次审查范围：当前分支相对 `main` 的已提交 diff；明确排除项
* 我理解的 PR 目标：一句话
* 系统承诺：1-5 条简述
* 主风险模型：1-3 个模型
* Coverage 状态：complete / incomplete
* Validation 状态：verified / partial / not-run / blocked
* 是否建议合并：是 / 修复后合并 / 补齐审查后再决定 / 不建议合并
* P0 数量：
* P1 数量：
* P2 数量：
* P3 数量：
* 最高风险领域：契约链路 / 多路径一致性 / 权限与身份 / 状态时序 / 性能 / 前端受控语义 / 测试 / 治理 / 无
* 最主要的风险概述：一到三句话
```

Coverage complete 且没有 finding 时，先输出：

```text
No actionable findings.
```

没有 confirmed finding 但 coverage incomplete 时，先输出：

```text
No confirmed findings, but review coverage is incomplete.
```

此时“是否建议合并”必须是“补齐审查后再决定”或更严格结论。
