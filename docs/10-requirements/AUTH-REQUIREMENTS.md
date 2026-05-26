# Auth Requirements

## Purpose

认证与权限模块定义平台账号派发、登录、角色、权限和审计的完整需求，是所有业务模块的访问控制基础。

## Scope

覆盖：
- 管理员派发账号。
- 用户登录和登出。
- 用户资料维护。
- 用户状态管理。
- admin、editor、viewer 三类基础角色。
- 业务操作权限判断。
- 关键操作日志。

不覆盖：
- 用户自助注册。
- 第三方 OAuth 登录。
- 邮件通知。
- 实时多人协作权限。


## Functional Requirements

- 必须支持管理员创建和派发账号。
- 必须支持用户使用已派发账号登录和登出。
- 必须支持用户查看和修改个人资料。
- 管理员必须能创建、编辑、禁用和删除用户。
- 必须支持 admin、editor、viewer 三类角色。
- 必须对内容查看、编辑、删除、导出、分享、批量操作、搜索和问答执行权限判断。
- 必须记录关键操作日志。
- 权限不足时必须给出明确提示。

## Business Rules

- 未登录用户不得访问需要登录的功能。
- 平台不得提供用户自助注册入口。
- 新账号必须由管理员创建并分配角色。
- admin 拥有全部管理权限。
- editor 可编辑授权范围内内容。
- viewer 只能查看授权范围内内容。
- 密码不得明文保存。
- 生产环境必须使用 HTTPS。
- 权限判断不得泄露用户无权访问的内容详情。
- 删除用户前必须二次确认。

## Acceptance Criteria

- 未登录用户访问受控功能时被要求登录。
- 权限不足用户无法查看、编辑、导出或分享无权内容。
- 管理员能禁用用户，禁用用户不能继续使用受控功能。
- 关键操作能在日志中追溯到操作人、时间、目标和结果。

## Related Documents

- [SEARCH-REQUIREMENTS.md](./SEARCH-REQUIREMENTS.md)：搜索结果必须消费本模块的权限判断。
- [QA-REQUIREMENTS.md](./QA-REQUIREMENTS.md)：问答上下文必须消费本模块的权限判断。
- [SHARING-REQUIREMENTS.md](./SHARING-REQUIREMENTS.md)：分享创建和私有分享访问必须消费本模块的权限判断。
- [SANCAI-KNOWLEDGE-REQUIREMENTS.md](./SANCAI-KNOWLEDGE-REQUIREMENTS.md)、[WANGQI-DOCUMENT-REQUIREMENTS.md](./WANGQI-DOCUMENT-REQUIREMENTS.md)、[MING-CUSTOMS-REQUIREMENTS.md](./MING-CUSTOMS-REQUIREMENTS.md)：内容查看、编辑、删除和公开私有状态管理必须消费本模块的权限判断。
- [OPERATIONS-REQUIREMENTS.md](./OPERATIONS-REQUIREMENTS.md)：使用关键操作日志和用户状态信息进行运维追溯。

## Open Items

无
