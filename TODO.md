# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
