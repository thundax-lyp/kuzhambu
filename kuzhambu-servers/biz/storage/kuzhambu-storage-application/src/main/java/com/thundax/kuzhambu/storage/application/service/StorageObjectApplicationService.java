package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageMimeTypesQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferenceOwnerTypesQuery;
import com.thundax.kuzhambu.storage.application.query.StorageObjectPageQuery;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import java.util.List;

public interface StorageObjectApplicationService {

    @LayerPublicApi(reason = "存储对象读取入口")
    StoredObject get(GetStorageObjectQuery query);

    @LayerPublicApi(reason = "存储对象列表读取入口")
    List<StoredObject> list(ListStorageObjectsQuery query);

    @LayerPublicApi(reason = "存储对象分页读取入口")
    PageResult<StoredObject> page(StorageObjectPageQuery query);

    @LayerPublicApi(reason = "存储对象元数据创建入口")
    StoredObject create(CreateStorageCommand command);

    @LayerPublicApi(reason = "存储对象元数据变更入口")
    void change(ChangeStorageCommand command);

    @LayerPublicApi(reason = "存储对象删除入口")
    int remove(RemoveStorageObjectCommand command);

    @LayerPublicApi(reason = "存储对象状态变更入口")
    int changeObjectStatus(ChangeStorageObjectStatusCommand command);

    @LayerPublicApi(reason = "存储对象排序入口")
    void sort(StorageSortCommand command);

    @LayerPublicApi(reason = "存储对象 MIME 类型列表入口")
    List<String> listMimeTypes(ListStorageMimeTypesQuery query);

    @LayerPublicApi(reason = "存储对象引用 owner 类型列表入口")
    List<String> listReferenceOwnerTypes(ListStorageReferenceOwnerTypesQuery query);
}
