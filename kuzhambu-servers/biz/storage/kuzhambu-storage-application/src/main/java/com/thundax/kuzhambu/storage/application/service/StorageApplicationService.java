package com.thundax.kuzhambu.storage.application.service;

import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.storage.application.command.AddStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageObjectStatusCommand;
import com.thundax.kuzhambu.storage.application.command.ChangeStorageReferenceStatusCommand;
import com.thundax.kuzhambu.storage.application.command.CreateStorageCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand;
import com.thundax.kuzhambu.storage.application.command.StorageSortCommand;
import com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand;
import com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery;
import com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery;
import com.thundax.kuzhambu.storage.application.query.StorageObjectPageQuery;
import com.thundax.kuzhambu.storage.application.query.StorageQuery;
import com.thundax.kuzhambu.storage.application.result.StoredObjectContentResult;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObject;
import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import java.util.List;

public interface StorageApplicationService {

    StoredObject get(GetStorageObjectQuery query);

    List<StoredObject> list(StorageQuery query);

    PageResult<StoredObject> page(StorageObjectPageQuery query);

    StoredObject create(CreateStorageCommand command);

    void sort(StorageSortCommand command);

    void change(ChangeStorageCommand command);

    int remove(RemoveStorageObjectCommand command);

    List<String> listMimeTypes(StorageQuery query);

    List<String> listReferenceOwnerTypes(StorageQuery query);

    int changeObjectStatus(ChangeStorageObjectStatusCommand command);

    int changeReferenceStatus(ChangeStorageReferenceStatusCommand command);

    int removeReferences(RemoveStorageReferencesCommand command);

    void addReferences(AddStorageReferencesCommand command);

    StoredObject upload(UploadStorageObjectCommand command);

    List<StoredObjectReference> listReferences(StorageQuery query);

    boolean existsReadableContent(StorageQuery query);

    StoredObjectContentResult openReadableContent(OpenReadableStorageContentQuery query);
}
