# 三才图会章节配图资源

本目录保存三才图会章节配图源文件，供切换环境后重新上传到 STORAGE 并绑定到三才图会条目。

## 文件说明

- `sancai-*.jpg`：可重新上传的图片源文件。
- `manifest.json`：图片说明、来源、目标条目和当前环境上传结果。

`manifest.json` 中的 `uploaded.assetId` 与 `uploaded.storageObjectId` 只代表生成该清单时的本地环境结果。切换数据库或 STORAGE 环境后，这两个 ID 会变化，应重新上传并以新环境返回值为准。

## 业务绑定

重传时按 `manifest.json` 中每条 `images[]` 执行：

- `file`：上传文件。
- `entryId`：目标三才图会条目。
- `title` / `description`：图片说明。
- `imageType`：固定使用 `ORIGINAL`。
- `currentUsed`：是否设为当前使用图。

业务上传接口：

```text
POST /kuzhambu-admin-api/api/classics/sancai/assets/images/upload
Content-Type: multipart/form-data
Access-Token: <admin token>
```

表单字段：

```text
entryId=<manifest.images[].entryId>
title=<manifest.images[].title>
imageType=ORIGINAL
currentUsed=<manifest.images[].currentUsed>
file=@<manifest.images[].file>
```

使用这个业务接口会同时创建 STORAGE 对象和三才图会图片绑定；不要只调用通用 STORAGE 上传接口，否则图片不会进入三才图会条目资源列表。

## 来源

- 故宫博物院《三才图会》页面提供卷首、总目和天文样图。
- 其他门类代表图来自国立国会图书馆数字馆藏 IIIF，《三才圖會 106卷》，manifest 标记为 `PDM`。
