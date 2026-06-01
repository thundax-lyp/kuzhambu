# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `AI A3`：实现提示词应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#a3-prompt-application`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/prompt/`
    - 处理动作：按 RUNBOOK A3 的 5 个关联文件实现提示词编辑、变量解析、版本对比、回滚和优化建议入口。
    - 验收点：保存或回滚后能刷新相关 AI 动作可用状态；变量缺失时拒绝保存或调用。
    - 重要度：9/10

- [ ] `AI A4`：实现 worker 调用应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#a4-worker-invocation-application`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/invocation/`
    - 处理动作：按 RUNBOOK A4 的 5 个关联文件实现 AI 域统一调用 workers 的同步和 SSE 编排。
    - 验收点：应用层能构造完整 worker 请求，保存调用记录，并把 completed 结果写为候选或返回调用方。
    - 重要度：10/10

- [ ] `AI A5`：实现内容精修应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#a5-refinement-application`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/refinement/`
    - 处理动作：按 RUNBOOK A5 的 4 个关联文件实现翻译、摘要、标签、问答、图片理解、视觉描述和条目拆分入口。
    - 验收点：AI 结果先进入候选区；拒绝候选不改变正式内容。
    - 重要度：9/10

- [ ] `AI A6`：实现批量任务应用服务
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#a6-batch-application`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/main/java/com/thundax/kuzhambu/ai/application/batch/`
    - 处理动作：按 RUNBOOK A6 的 4 个关联文件实现批量任务创建、单元派发、失败归档和取消语义。
    - 验收点：取消后不再派发未开始 worker 调用；已完成结果保留。
    - 重要度：8/10

- [ ] `AI I1`：实现配置与模型持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#i1-config-and-model-persistence`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/model/`
    - 处理动作：按 RUNBOOK I1 的 4 个关联文件实现服务配置、模型和检测记录持久化。
    - 验收点：DO 字段与 `db/schema/ai.sql` 一致，Repository 不泄漏 MyBatis 细节。
    - 重要度：9/10

- [ ] `AI I2`：实现能力与提示词持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#i2-capability-and-prompt-persistence`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/capability/`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/prompt/`
    - 处理动作：按 RUNBOOK I2 的 4 个关联文件实现能力、能力映射、动作状态和提示词持久化。
    - 验收点：当前提示词只通过 `scope + capability` 定位，版本历史可查询和回滚。
    - 重要度：9/10

- [ ] `AI I3`：实现调用记录持久化
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#i3-invocation-persistence`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/invocation/`、`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/batch/`
    - 处理动作：按 RUNBOOK I3 的 4 个关联文件实现调用记录、候选结果、批量任务和专项结果持久化。
    - 验收点：调用失败、stream 中断、候选状态和批量计数均能持久化。
    - 重要度：9/10

- [ ] `AI I4`：实现 worker client 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#i4-worker-client-contract`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/main/java/com/thundax/kuzhambu/ai/infra/client/`
    - 处理动作：按 RUNBOOK I4 的 5 个关联文件实现 `WORKERS-AI-INTERFACE.md` 定义的 HTTP、SSE、HMAC 和错误归一化。
    - 验收点：请求头、签名输入、错误类型和 stream completed 处理与接口文档一致。
    - 重要度：10/10

- [ ] `AI F1`：实现后台配置接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#f1-admin-config-interface`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/config/`
    - 处理动作：按 RUNBOOK F1 的 4 个关联文件提供 AI 服务、模型、模型检测和能力映射后台接口。
    - 验收点：后台能配置主备服务、模型、能力映射，并查看检测历史。
    - 重要度：8/10

- [ ] `AI F2`：实现后台提示词接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#f2-admin-prompt-interface`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/prompt/`
    - 处理动作：按 RUNBOOK F2 的 4 个关联文件提供提示词编辑、变量解析、版本对比、回滚和动作状态接口。
    - 验收点：管理员能管理提示词版本并看到相关动作是否可用。
    - 重要度：8/10

- [ ] `AI F3`：实现后台调用接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#f3-admin-invocation-interface`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/invocation/`
    - 处理动作：按 RUNBOOK F3 的 4 个关联文件提供 AI 调用统计、调用记录、批量任务和候选管理接口。
    - 验收点：管理员能查看调用延迟、失败、成本、批量状态和失败原因。
    - 重要度：8/10

- [ ] `AI F4`：实现后台精修接口
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#f4-admin-refinement-interface`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-interface/src/main/java/com/thundax/kuzhambu/ai/interfaces/admin/refinement/`
    - 处理动作：按 RUNBOOK F4 的 4 个关联文件提供 Classics 内容上下文可调用的 AI 精修接口。
    - 验收点：翻译、摘要、标签、问答对、图片理解和条目拆分可进入候选区。
    - 重要度：8/10

- [ ] `AI S1`：装配后台 starter
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#s1-admin-starter-assembly`
    - 范围对象：`kuzhambu-servers/starter/kuzhambu-admin-starter/`、`.env.example`、`deploy/.env.example`
    - 处理动作：按 RUNBOOK S1 的 4 个关联文件装配 AI interface、application、infra 和 worker client 配置。
    - 验收点：后台 starter 能扫描 AI 模块，worker endpoint 和 HMAC secret 通过环境变量配置。
    - 重要度：8/10

- [ ] `AI V1`：补齐 AI 架构验证
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#v1-ai-architecture-verification`
    - 范围对象：`kuzhambu-servers/biz/ai/*/src/test/java/com/thundax/kuzhambu/ai/`
    - 处理动作：按 RUNBOOK V1 的 4 个关联文件新增或补齐 AI 模块架构验证。
    - 验收点：架构测试覆盖层依赖、包路径、Controller、Repository、DO 和 Mapper 归属。
    - 重要度：7/10

- [ ] `AI V2`：验证 worker client 协议
    - 任务类型：执行任务
    - 依据文档：`docs/30-designs/RUNBOOK-AI-DOMAIN.md#v2-worker-client-verification`
    - 范围对象：`kuzhambu-servers/biz/ai/kuzhambu-ai-infra/src/test/java/com/thundax/kuzhambu/ai/infra/client/`、`kuzhambu-servers/biz/ai/kuzhambu-ai-application/src/test/java/com/thundax/kuzhambu/ai/application/invocation/`
    - 处理动作：按 RUNBOOK V2 的 3 个关联文件验证 worker client 请求签名、错误归一化和 stream completed 处理。
    - 验收点：签名输入与接口文档一致；stream 未收到 completed 时记录为失败或部分失败。
    - 重要度：9/10

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
