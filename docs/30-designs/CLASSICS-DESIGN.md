# Classics Design

## Purpose

本文档定义 Classics 古籍域设计，覆盖三才图会、王圻文档、明代习俗、导出和分享。

## Module

```text
kuzhambu-servers/biz/classics/
  kuzhambu-classics-interface/
  kuzhambu-classics-application/
  kuzhambu-classics-domain/
  kuzhambu-classics-infra/
```

## Business Boundary

Classics 拥有古籍内容主数据、内容版本、可见性、生命周期、导出记录、分享链接和分享目标。AI、Knowledge、Discovery 只能通过 Classics application 能力消费内容上下文，不得直接写入 Classics 主表。

## DDD Model

- `SancaiCategory`
- `SancaiVolume`
- `SancaiEntry`
- `SancaiImage`
- `VisualAsset`
- `WangqiDocument`
- `MingCustom`
- `ContentSummary`
- `ContentTag`
- `ContentQaPair`
- `ContentVersion`
- `ContentExport`
- `ShareLink`
- `ShareTarget`
- `ShareAccessRecord`

## Data Model

表名前缀统一使用 `classics_`。

核心表：

- `classics_sancai_category`
- `classics_sancai_volume`
- `classics_sancai_entry`
- `classics_sancai_image`
- `classics_visual_asset`
- `classics_wangqi_document`
- `classics_ming_custom`
- `classics_content_summary`
- `classics_content_tag`
- `classics_content_qa_pair`
- `classics_content_version`
- `classics_export_record`
- `classics_share_link`
- `classics_share_target`
- `classics_share_access_record`

## Application Layer

- `SancaiApplicationService`
- `WangqiDocumentApplicationService`
- `MingCustomApplicationService`
- `VisualAssetApplicationService`
- `ContentVersionApplicationService`
- `ContentExportApplicationService`
- `SharingApplicationService`

Application 层负责内容生命周期、权限过滤、批量操作、版本记录、导出任务、分享访问控制和 Storage 引用协调。

## Interface Layer

Admin 入口：

- 内容 CRUD、批量操作、版本恢复、导出、分享管理和视觉资产工作流。

Portal 入口：

- 公开或授权内容只读浏览。
- 分享访问页。
- 静态展示页面生成产物不等同于 portal 权限控制。

## Infrastructure Layer

- Repository 持久化 `classics_*` 表。
- Storage 文件对象只保存稳定文件对象标识。
- HTML 设定集、视觉资产设定集和静态展示页面模板作为系统静态资源发布。

## Data Ownership

Classics 是 `classics_*` 表的唯一写入方。内容可见性、归档、分享访问和导出范围由 Classics 判断。

## Observability

- 内容写操作通过 System 业务审计记录。
- 分享访问记录由 Classics 保存。
- 批量操作记录成功数、失败数和失败原因。

## Acceptance

- 三类古籍内容在同一业务域内复用版本、可见性、导出和分享能力。
- 私有、归档、删除和分享状态互不隐式修改。
