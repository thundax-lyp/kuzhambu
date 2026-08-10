# ArchUnit allowlist 清理 05：System Core

## Purpose

清理 System core（用户、角色、菜单、部门、字典及个人资料）切片的 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| core Command/Query record 例外 | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java` |
| core ApplicationService 边界与构造位置例外 | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java` |
| CurrentUser、Department、Menu、Role、User Controller 动词及相关 Assembler 例外 | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java` |

## Non-goals

不处理 auth、audit 条目；不修改共享 ArchUnit support。

## Plan

1. 先处理 04 批次评审遗留的登录 smoke 文档路由同步：
   - 目标文件：`docs/00-governance/HOW-TO-ADMIN-LOGIN-SMOKE.md`
   - 旧路由：`/api/auth/session/pre-auth-session`
   - 新路由：`/api/auth/session/pre-auth-session/request`
   - 触发原因：04 批次将 pre-auth creation 映射迁移到 `pre-auth-session/request` 后，登录 smoke guide 的说明和 curl 命令仍指向旧路由，会导致按文档执行登录冒烟时在预认证步骤返回 404。
2. 仅移除 `application.core` 与对应 Controller/Assembler 的 key。
3. 随生产代码整改同步删除例外。
4. 对 HTTP 路径或 service 方法改名同步更新调用方。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

System core 条目清零后删除本文档。
