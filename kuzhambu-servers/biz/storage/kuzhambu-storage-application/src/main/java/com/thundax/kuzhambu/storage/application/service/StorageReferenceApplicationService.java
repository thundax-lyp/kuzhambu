package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.arch.LayerPublicApi;
import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.query.ListStorageReferencesQuery;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import java.util.List;

public interface StorageReferenceApplicationService {

    @LayerPublicApi(reason = "存储对象引用列表读取入口")
    List<StoredObjectReference> list(ListStorageReferencesQuery query);

    @LayerPublicApi(reason = "业务对象保存文件后写入存储引用关系的跨模块入口")
    void addReferences(AddStorageReferencesCommand command);

    @LayerPublicApi(reason = "业务对象删除或解绑时清理存储引用关系的跨模块入口")
    int removeReferences(RemoveStorageReferencesCommand command);

    @LayerPublicApi(reason = "存储对象引用状态变更入口")
    int changeReferenceStatus(ChangeStorageReferenceStatusCommand command);
}
