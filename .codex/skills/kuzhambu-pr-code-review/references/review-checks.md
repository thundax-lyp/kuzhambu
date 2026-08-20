# PR Review Checks

先对全部 changed hunks 执行必经基础检查。运行时模型、强制专项和模块检查只在当前 PR 的系统承诺、主风险模型或 changed hunk 真实触发时执行；关键词仅用于定位，没有语义变化时不要扩大审查范围。

## 必经基础检查

逐个检查所有 changed hunks，不得因为它没有命中专项失效模型而跳过：

- **正确性**：逻辑、边界、空值、空集合、非法状态、错误转换和异常吞噬是否会让调用方误判；
- **运行时完整性**：并发、生命周期、事务、幂等、资源释放、失败恢复和配置默认值是否保持明确语义；
- **安全性**：输入与权限校验、注入、路径遍历、不安全反序列化、不可信文件或 URL、敏感信息和日志脱敏；
- **兼容性**：既有数据、接口、枚举、配置、迁移、调用方和失败语义是否仍兼容；
- **架构与维护性**：模块归属、依赖方向和职责边界是否造成真实回归风险或后续维护阻塞。

基础检查发现的问题同样必须满足 finding 门槛。只有 changed-file ledger 能证明所有 changed hunks 均完成基础检查时，coverage 才可能 complete。

## 运行时模型

### 契约链路

字段默认值、可空性、枚举、schema、配置来源、状态或 request/response 契约变化时，记录：

- source of truth；
- producer；
- adapter、assembler 或 wrapper；
- 首个真实 validator；
- 最终 consumer/sink；
- fallback、历史值和失败发生位置；
- 覆盖真实消费行为的测试。

### 多路径一致性

同一能力存在 sync/async、create/update、list/detail、preview/apply、dialog/page/new-tab、不同 taskType 或 status 时，对账各路径的数据、状态、权限、action 和 fallback 语义。

### 状态与时间线

任务、版本、候选、apply、轮询、refetch、fallback、local draft 或 effect 同步变化时，至少推演：

1. 旧数据完成后新数据开始；
2. 新数据开始后旧数据才回写或 apply；
3. taskType、section、tab、对象或 refresh 交错。

### 前端状态边界

表单、section、drawer、modal、hook 或 wrapper 变化时，确认：

- 真实 draft state 的 owner；
- 展示、patch 与提交各自的 owner；
- named form item 的 direct child 或 wrapper 是否透明转发受控协议；
- `initialValues`、`setFieldsValue`、`resetFields` 的职责；
- refetch、mutation success、对象或 tab 切换是否覆盖草稿；
- child unmount 后 parent 是否回退到错误的 live/fallback state；
- effect 依赖中的 callback 和 object identity 是否稳定。

### 权限与身份

菜单、按钮、route、role、permission、subject、owner、token 或资源 URL 变化时，对账 UI 门控、服务端认证主体、后端鉴权、seed 展开逻辑以及 preview/download/new-tab 的认证语义。客户端传入的 actor、tenant、scope、owner 或 subjectId 不能替代服务端身份事实源。

### 数据范围与性能

搜索、树、列表、分页、聚合、跨域 facade、React Query key 或循环内调用变化时，检查：

- O(1) 或 O(page) 是否放大为 O(N) 或 O(N * remote)；
- root-only、空过滤、快速输入或大结果集是否触发全量工作；
- 是否需要 debounce、batch、memoize、skip 或条件查询；
- 缓存 key 是否遗漏会改变结果的参数。

### 治理与交付

门禁、CI、脚本、agent、skill、PR 或发布流程变化时，确认其约束对象、实际输入、放行/拦截/跳过行为、仓库样本，以及失败、中断、远端变化或信息过期后的停止和恢复语义。

## 强制专项检查

### Producer-to-sink tracing

changed hunk 改变 Command、Request、Response、DTO、Result、schema、prompt variables、配置 key、seed、权限字段、资源 URL、taskType 或 status 的运行时语义时：

- 用 `rg` 搜索字段、常量或 key 的所有读取点；
- 找到首个真实 validator 和最终 consumer/sink；
- 检查持久化历史值和旧枚举；
- 确认测试覆盖消费点，而不只是构造点。

### Form direct-child / wrapper transparency

`Form.Item`、`KuzhambuFormItem` 子树或输入 wrapper 变化时，检查 named item 的 direct child，或 wrapper 对 `value/onChange`、`checked/onChange` 及自定义控件协议的完整转发。

### Partial patch / refresh / draft

`initialValues`、`setFieldsValue`、`resetFields`、写表单的 effect、refetch 或 mutation success 变化时，同时推演同一对象刷新和切换对象，检查 omitted/null 字段、partial payload、NON_NULL 响应、旧值残留和未保存草稿覆盖。

### Permission and authenticated resources

权限、身份、token、download、preview 或 new-tab 路径变化时，检查前后端门控一致性、服务端主体来源、资源请求认证和 seed 的真实消费逻辑。

### Performance amplifiers

搜索、树、列表、分页、聚合、循环调用或 query key 变化时，检查 root/empty/typing/large-result 场景和请求数量级。

## 模块检查

### Java servers

- Controller、application、domain、infra 的依赖方向和模块归属；
- 写操作事务完整性，以及事务内不必要的外部 IO；
- Repository 的权限、业务范围、生命周期、分页、排序和缓存真相源；
- DTO、command、response 与 frontend、worker、公开接口的兼容性；
- 强类型 ID、ULID、token、外部 ID 的语义；
- 幂等、重复提交、并发更新、异常和恢复语义。

### Frontend apps

- 页面是 orchestration 还是 capability，业务状态和 service call 是否由正确层级拥有；
- 表单受控语义、本地 draft、effect identity、异步刷新和切换对象；
- route、preview、download、modal 和 new-tab 的权限及认证；
- admin table action 结构和 flexible data column；
- 测试是否验证用户可观察行为。

### Python workers

- capability 输入输出与 `docs/20-interfaces/` 一致；
- worker 不解析 Java backend 所有的业务 JSON 协议；
- 流式事件顺序、结束、错误和 partial result；
- timeout、retry、cancel 和异常回传不会让调用方误判成功或挂起；
- 大文件、大响应、长任务的资源上限和清理。

### Cross-layer AI flows

- backend capability 使用 Java enum name，不把 worker canonical value 写入业务持久化面；
- queued/batch task 优先使用提交时捕获的 model、prompt、messages、variables、schema 和 params snapshot；
- prompt rendering、JSON compatibility extraction、structured validation 和业务协议解析由 Java backend 完成；
- capability rename 同步检查 seed/config、历史、candidate、invocation、batch、frontend、worker mapping 和文档。

## 测试与负空间

- 检查失败、权限、历史数据、刷新、交错时序、重复执行和兼容路径，而不只看 happy path。
- passing checks 是证据，不是行为正确的证明。
- 未运行测试本身不自动成为 finding；只有缺失验证导致具体回归风险时才报告，并说明缺失的行为路径。
