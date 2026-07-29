package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import java.util.List;

public interface StorageApplicationOperations {

    StoredObject get(StoredObjectId id);

    List<StoredObject> list(StorageQuery query);

    PageResult<StoredObject> page(StorageQuery query, PageQuery page);

    StoredObjectId create(CreateStorageCommand command);

    void change(ChangeStorageCommand command);

    int remove(StoredObjectId id);

    List<String> listMimeTypes(StorageQuery query);

    List<String> listReferenceOwnerTypes(StorageQuery query);

    int changeObjectStatus(ChangeStorageObjectStatusCommand command);

    int changeReferenceStatus(ChangeStorageReferenceStatusCommand command);

    @LayerPublicApi(reason = "业务对象删除或解绑时清理存储引用关系的跨模块入口")
    int removeReferences(RemoveStorageReferencesCommand command);

    @LayerPublicApi(reason = "业务对象保存文件后写入存储引用关系的跨模块入口")
    void addReferences(AddStorageReferencesCommand command);

    @LayerPublicApi(reason = "业务对象上传存储对象内容并落元数据的跨模块入口")
    StoredObject upload(UploadStorageObjectCommand command);

    List<StoredObjectReference> listReferences(StorageQuery query);

    @LayerPublicApi(reason = "存储对象下载或预览前校验内容可读性的业务入口")
    boolean existsReadableContent(StorageQuery query);

    @LayerPublicApi(reason = "存储对象内容读取的业务入口")
    StoredObjectContentResult openReadableContent(StoredObjectId id);

    void sort(StorageSortCommand command);
}
