# ArchUnit allowlist 清理 04：System 认证与审计

## Purpose

清理 System 域认证、会话、权限与审计切片的 legacy allowlist。

## Scope

| allowlist 项目 | 所在文件 |
| --- | --- |
| auth、audit Command/Query record 例外 | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationCommandQueryRecordAllowances.java` |
| auth、audit ApplicationService 边界与构造位置例外 | `kuzhambu-servers/biz/system/kuzhambu-system-application/src/test/java/com/thundax/kuzhambu/system/application/SystemApplicationArchitectureTest.java` |
| Auth、Captcha、Audit Controller 动词及相关 Assembler 例外 | `kuzhambu-servers/biz/system/kuzhambu-system-interface/src/test/java/com/thundax/kuzhambu/system/interfaces/SystemInterfaceArchitectureTest.java` |

## Non-goals

不处理 System core（用户、角色、菜单、部门、字典）条目；不改变认证协议的对外兼容性而未同步调用方。

## Plan

1. 仅移除 key 前缀或 FQCN 属于 `auth`、`audit` 的条目。
2. 将 Command/Query 迁移为 record，并将不合法构造点移至合法边界。
3. Controller 动词变更同步更新 HTTP 调用方。

## Verification

在 `kuzhambu-servers/` 下运行：`mvn -pl biz/system/kuzhambu-system-application,biz/system/kuzhambu-system-interface -am test`，再执行 `mvn spotless:check`、`mvn checkstyle:check`。

## Closure

认证与审计条目清零后删除本文档。
