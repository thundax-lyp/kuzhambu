# PR Review Failure Models

从以下类别中选择 1-3 个与当前 PR 最相关的主风险模型。

## A. 契约链路失效

适用于 DTO、command、request、response、默认值、可空性、枚举、schema、prompt 变量、配置 key 或输出结构变化。

常见症状：上游字段变化后真实消费点未同步；中间层只转发不校验；测试只覆盖构造结果。

## B. 多路径不一致

适用于同一能力的 sync/async、create/update、preview/apply、list/detail、page/dialog、taskType、status、version、candidate 或 action 路径。

常见症状：展示与操作语义不同；只修复一条等价路径；preview、download 或 new-tab 的认证和数据语义不一致。

## C. 权限与身份真相源错位

适用于菜单、按钮、资源链接、controller、subject、role、permission、seed，或客户端传入 actor、tenant、scope、owner、subjectId。

常见症状：前端可见但后端拒绝；权限 seed 意外扩大权限；身份来自客户端而非认证主体。

## D. 状态机与时序失效

适用于任务、版本、候选、apply、异步回写、刷新、轮询、fallback 或局部状态同步。

常见症状：旧数据晚到覆盖新状态；unmount 后停留在旧状态；局部状态与远端刷新冲突。

## E. 数据范围与性能放大

适用于搜索、树、列表、分页、聚合、逐项查询、React Query key 或循环内 service/repository/facade 调用。

常见症状：root 页面全量扫描；每次输入触发昂贵请求；每条记录产生独立数据库或远程调用。

## F. 前端受控语义与局部状态边界失效

适用于表单 wrapper、自定义字段、`initialValues`、`setFieldsValue`、`resetFields`、effect、本地 draft、refetch 或 mutation success。

常见症状：wrapper 丢失受控协议；刷新覆盖草稿；partial patch 遗留旧值；不稳定依赖触发循环更新。

## G. 测试伪覆盖

适用于只增加 happy-path 测试、只断言构造数据，或缺少刷新、失败、历史数据和交错时序测试。

常见症状：测试全绿但非默认路径失败；测试绑定实现细节而未覆盖用户可观察行为。

## H. 治理能力失效

适用于架构测试、命名门禁、CI、验证脚本、PR 流程、agent、skill、自动化或发布规则。

常见症状：只检查文件形状而未检查真实运行对象；能被等价路径绕过；基于过期状态报告成功；失败后无法恢复到可审查状态。
