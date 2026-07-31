# Classics Content Version Snapshot Interface

## Purpose

本文档固定古籍正式内容版本 `classics_content_version.snapshot_json` 的稳定 JSON 契约。

该快照用于版本历史、历史恢复和 Portal 已发布稿件展示。Portal 读取当前已发布稿件，不维护脱离内容生命周期的外发快照。

## Common Rules

- `snapshot_json` 是 JSON object。
- 字段名使用 lower camel case。
- 时间字段使用 ISO-8601 instant 字符串，例如 `2026-06-20T10:00:00Z`。
- 枚举字段使用后端业务枚举名字符串，不序列化 Java enum object。
- ID 字段使用 number；缺失关联使用 `null`。
- 允许字段值为 `null`，但不得省略本文档列出的字段。
- 不包含 Java 类名、包名、内部对象结构或审计字段。
- 三类快照均包含 `lifecycleStatus`、`tags` 和 `qaPairs`，且不包含 `visibility`。
- `tags` 只包含 ACTIVE 标签，按 `priority ASC` 固定顺序。
- `qaPairs` 只包含 question、answer 均非空白的有效问答，按 `priority ASC` 固定顺序。

## SANCAI_ENTRY

`contentType` 固定为 `SANCAI_ENTRY`。

```json
{
  "contentType": "SANCAI_ENTRY",
  "contentId": 300000003360,
  "contentUpdatedAt": "2026-06-20T10:00:00Z",
  "volumeId": 1,
  "volumeTitle": "天部",
  "categoryId": 10,
  "categoryTitle": "天文",
  "title": "天地",
  "originalText": "原文",
  "translationText": "译文",
  "summary": "摘要",
  "lifecycleStatus": "PUBLISHED",
  "translationStatus": "PENDING",
  "imageStatus": "PENDING",
  "visualAssetStatus": "PENDING",
  "refinementStatus": "PENDING",
  "priority": 1,
  "images": [
    {
      "imageId": 8002,
      "storageObjectId": 2001,
      "originalFilename": "三才图.png",
      "contentType": "image/png",
      "size": 102400,
      "imageType": "ORIGINAL",
      "title": "天地图",
      "currentUsed": true,
      "priority": 1
    },
    {
      "imageId": 8003,
      "storageObjectId": 2002,
      "originalFilename": "三才生成图.png",
      "contentType": "image/png",
      "size": 204800,
      "imageType": "GENERATED",
      "title": "天地图生成稿",
      "currentUsed": false,
      "priority": 2
    }
  ],
  "tags": [],
  "qaPairs": []
}
```

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentType` | string | 内容类型，固定 `SANCAI_ENTRY` |
| `contentId` | number/null | 三才条目 ID |
| `contentUpdatedAt` | string/null | 主内容业务更新时间 |
| `volumeId` | number/null | 所属卷 ID |
| `volumeTitle` | string/null | 创建快照时的所属卷标题 |
| `categoryId` | number/null | 创建快照时的所属分类 ID |
| `categoryTitle` | string/null | 创建快照时的所属分类标题 |
| `title` | string/null | 标题 |
| `originalText` | string/null | 原文 |
| `translationText` | string/null | 译文 |
| `summary` | string/null | 摘要 |
| `lifecycleStatus` | string/null | 生命周期状态 |
| `translationStatus` | string/null | 翻译状态 |
| `imageStatus` | string/null | 图片状态 |
| `visualAssetStatus` | string/null | 视觉资产状态 |
| `refinementStatus` | string/null | 精修状态 |
| `priority` | number | 卷内排序值 |
| `images` | array | 三才条目图片资源快照，包含该条目的全部图片，按 `priority ASC` 排序 |
| `tags` | array | ACTIVE 标签快照 |
| `qaPairs` | array | 有效问答快照 |

`images[]` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `imageId` | number/null | 三才图片记录 ID |
| `storageObjectId` | number/null | 图片 Storage 对象 ID |
| `originalFilename` | string/null | 创建快照时的原始文件名 |
| `contentType` | string/null | 创建快照时的内容类型 |
| `size` | number/null | 创建快照时的文件大小，单位字节 |
| `imageType` | string/null | 图片类型 |
| `title` | string/null | 图片标题 |
| `currentUsed` | boolean | 是否当前使用图。`true` 标识当前展示主图，`false` 标识同条目其他备选图 |
| `priority` | number | 图片展示排序值 |

`SANCAI_ENTRY.images` 不包含 `previewUrl` 或 `downloadUrl`，不承诺新增数据库字段。Portal 响应层会根据 `storageObjectId` 动态装配资源对象和读取 URL。当前图变化只影响后续版本、导出和展示 payload。

## WANGQI_DOCUMENT

`contentType` 固定为 `WANGQI_DOCUMENT`。

```json
{
  "contentType": "WANGQI_DOCUMENT",
  "contentId": 1001,
  "contentUpdatedAt": "2026-06-20T10:00:00Z",
  "title": "文档标题",
  "summary": "摘要",
  "contentFormat": "MARKDOWN",
  "content": "正文",
  "documentTime": "2026-06-20T10:00:00Z",
  "storageObjectId": 2001,
  "lifecycleStatus": "PUBLISHED",
  "tags": [],
  "qaPairs": []
}
```

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentType` | string | 内容类型，固定 `WANGQI_DOCUMENT` |
| `contentId` | number/null | 王圻文档 ID |
| `contentUpdatedAt` | string/null | 主内容业务更新时间 |
| `title` | string/null | 标题 |
| `summary` | string/null | 摘要 |
| `contentFormat` | string/null | 正文格式 |
| `content` | string/null | 正文内容 |
| `documentTime` | string/null | 文档时间 |
| `storageObjectId` | number/null | 原始文件对象 ID |
| `lifecycleStatus` | string/null | 生命周期状态 |
| `tags` | array | ACTIVE 标签快照 |
| `qaPairs` | array | 有效问答快照 |

## MING_CUSTOMS

`contentType` 固定为 `MING_CUSTOMS`。

```json
{
  "contentType": "MING_CUSTOMS",
  "contentId": 1001,
  "contentUpdatedAt": "2026-06-20T10:00:00Z",
  "title": "习俗标题",
  "category": "岁时",
  "chapter": "卷一",
  "section": "小节",
  "summary": "摘要",
  "contentFormat": "MARKDOWN",
  "content": "正文",
  "originalExcerpts": "原文摘录",
  "lifecycleStatus": "PUBLISHED",
  "tags": [],
  "qaPairs": []
}
```

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentType` | string | 内容类型，固定 `MING_CUSTOMS` |
| `contentId` | number/null | 明代习俗条目 ID |
| `contentUpdatedAt` | string/null | 主内容业务更新时间 |
| `title` | string/null | 标题 |
| `category` | string/null | 分类 |
| `chapter` | string/null | 章节 |
| `section` | string/null | 小节 |
| `summary` | string/null | 摘要 |
| `contentFormat` | string/null | 正文格式 |
| `content` | string/null | 正文内容 |
| `originalExcerpts` | string/null | 原文摘录 |
| `lifecycleStatus` | string/null | 生命周期状态 |
| `tags` | array | ACTIVE 标签快照 |
| `qaPairs` | array | 有效问答快照 |

## Tag And QA Shapes

三类快照的 `tags[]` 字段固定为：

| 字段 | 类型 |
| --- | --- |
| `id` | number/null |
| `tagId` | number/null |
| `tagNameSnapshot` | string/null |
| `source` | string/null |
| `status` | string/null，固定为 `ACTIVE` |
| `priority` | number/null |

三类快照的 `qaPairs[]` 字段固定为：

| 字段 | 类型 |
| --- | --- |
| `id` | number/null |
| `question` | string |
| `answer` | string |
| `source` | string/null |
| `priority` | number/null |

## Mapping Ownership

正式版本快照由 `ClassicsContentSnapshotAssembler` 统一生成，三类 DTO 分别为：

- `SancaiEntryVersionSnapshot`
- `WangqiDocumentVersionSnapshot`
- `MingCustomsVersionSnapshot`

Portal 详情只展示当前已发布稿件对应的正式版本快照；Portal API 不从前端请求体重新组装展示数据。

对外响应可以在快照稳定 ID 的基础上动态补资源对象，例如 `WANGQI_DOCUMENT.storageObjectId` 可装配为 `target.storageObject`，`SANCAI_ENTRY.images[].storageObjectId` 可装配为 `target.images[].storageObject`。该装配结果不回写 `snapshot_json`。
