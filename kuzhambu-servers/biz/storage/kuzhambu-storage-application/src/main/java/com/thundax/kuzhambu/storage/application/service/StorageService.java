package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.application.entity.StoredObject;
import com.thundax.kuzhambu.storage.application.entity.StoredObjectReference;
import com.thundax.kuzhambu.storage.application.service.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.service.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.service.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.service.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.service.query.StorageQuery;
import com.thundax.kuzhambu.storage.domain.object.valueobject.StoredObjectId;
import java.util.List;

public interface StorageService {

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

    List<StoredObjectReference> listReferences(StorageQuery query);

    @LayerPublicApi(reason = "存储对象下载或预览前校验内容可读性的业务入口")
    boolean existsReadableContent(StorageQuery query);

    void sort(StorageSortCommand command);
}
