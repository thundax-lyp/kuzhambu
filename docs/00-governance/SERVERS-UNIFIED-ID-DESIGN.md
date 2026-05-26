# Unified ID Design

## Purpose

本文档定义 kuzhambu 统一业务标识的工程边界和默认写法。

## Scope

- 统一 `UserId`、`RoleId`、`MenuId`、`IdentityId`、`CredentialId`、`ObjectId` 等领域标识建模。
- 保留类型安全。
- 统一 Jackson、MyBatis 和持久化边界适配。
- 不覆盖业务单号和独立随机 token 设计。

## Core Distinction

- `Database Primary Key`：数据库内部主键，默认 `bigint id`。
- `Domain Identifier`：Java 里的强类型领域标识，例如 `UserId`、`RoleId`。
- `Business No`：业务单号，例如后续可能出现的任务编号、导入批次号。
- `Random Token`：独立随机访问凭证，例如分享链接、下载凭证、access token、refresh token。

四者不得混用。

## Module Boundary

- 统一 ID 公共能力固定放在 `kuzhambu-common-core`。
- 统一 ID 包路径固定为 `com.thundax.kuzhambu.common.core.id`。
- `domain` 必须使用强类型 ID；除明确列入豁免类名白名单的类型外，不得使用基础类型表达业务对象标识。
- `interfaces`、接口 DTO、infra DO 可按协议和持久化需要保留基础类型。
- 边界转换必须通过明确的 assembler、converter、codec 或 TypeHandler 完成。
- 独立随机 token 不属于统一 ID，不得由业务 ULID 直接暴露替代。

## Core Model

- `Identifier<T>`：标识接口。
- `BaseId<T>`：抽象基类。
- 具体 ID：例如 `UserId`、`RoleId`、`MenuId`、`IdentityId`。
- `IdGenerator`：统一底层值生成器。
- `Ids`：统一工厂入口。
- `IdConverters` 或 TypeHandler：统一适配层。

## Hard Rules

- 统一业务对象对外标识采用 ULID。
- 统一 ID 采用“单值包装 + 强类型”模型。
- `BaseId<T>` 必须不可变。
- 构造器固定非公开，统一使用 `of(...)`。
- 判等必须基于具体类型和底层值。
- 不允许每个具体 ID 重写 `equals`、`hashCode`、`toString`。
- 框架适配逻辑固定放在转换器或 TypeHandler。
- `domain` 不得被 MyBatis、JPA、Spring MVC 注解污染。
- 新增 ID 类型时，优先复用统一基类和统一转换器。

## Value Type Rules

- 文本型 ID 底层值不得为空白。
- 一个具体 ID 类型只能固定一种底层值类型。
- Core 用户、角色、菜单标识默认使用 `String`。
- Auth 登录标识、凭据、认证事件标识默认使用 `String`。
- Storage 对象标识默认使用 `String`。
- 数据库字段默认使用 `varchar(64)` 保存文本型领域标识。

## Generation Rules

- `Ids` 是统一工厂入口。
- `Ids` 负责屏蔽不同发号来源。
- `Ids` 负责把底层值包装成具体 ID。
- 同一种 ID 的业务标签必须稳定。
- 发号提供方返回非法结果时必须失败，不能静默兜底。

默认风格：

```java
UserId userId = UserId.of("01J00000000000000000000000");
RoleId roleId = ids.roleId();
MenuId menuId = ids.menuId();
```

## Serialization And Persistence

- Jackson 对外序列化为单一基础值。
- 不序列化成额外包裹结构，例如 `{"value":"..."}`。
- MyBatis 通过统一 TypeHandler 或转换器完成持久化。
- 同一业务域内同一类 ID 的持久化方式必须统一。

## Migration Rules

- 只在语义明显不匹配时调整数据库结构。
- 数据库、代码、文档必须先统一“这是主键、领域标识、业务单号还是随机 token”。
- 不允许把统一 ID 改造理解成全库字段类型统一重写。

## Non-Functional Rules

- 统一 ID 基础设施必须在公共模块，不能每个业务域各写一套。
- 新增具体 ID 类型时，除类型声明外的新增代码应控制在极小范围。
- 统一 ID 不得明显增加序列化和持久化复杂度。
- 必须支持单元测试直接构造，不依赖 Spring 容器。
