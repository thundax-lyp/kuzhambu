# Module Design Template

## Purpose

本文档是业务域独立设计文档模板。每个业务域设计文档必须能独立成文，不依赖总平台需求或其他业务域文档才能理解本域边界。

## Module

- 模块名称：
- 业务域：
- 对应需求文档：
- 后端 biz 子工程：
- 后端 infra 子工程：
- 后台接口入口：
- 前台接口入口：
- 前端入口：
- Python worker 能力：

## Business Boundary

- 本模块负责：
- 本模块不负责：
- 依赖的其他业务域能力：
- 对外提供的业务能力：

## Application Layer

- `*ApplicationService`：
- 命令对象：
- 查询对象：
- 结果对象：
- 事务边界：
- 幂等规则：

## Domain Layer

- 领域对象：
- 领域服务：
- 业务规则：
- 领域异常：
- 本域 ErrorCode 前缀：

## Interface Layer

- 后台接口：
- 前台接口：
- 权限要求：
- DTO 与展示口径：
- OpenAPI 分组：

## Infrastructure Layer

- Repository：
- Mapper：
- PersistenceAssembler：
- 外部客户端：
- 缓存：
- 消息：
- 搜索索引：
- 文件存储：

## Data Ownership

- 本模块拥有的数据：
- 本模块只读引用的数据：
- 禁止跨域直接访问的数据：
- Flyway 脚本归属：

## Observability

- 运行日志：
- 访问日志：
- 审计日志：
- 关键指标：

## Acceptance

- 功能验收：
- 权限验收：
- 数据验收：
- 接口验收：
- 回归验证：
