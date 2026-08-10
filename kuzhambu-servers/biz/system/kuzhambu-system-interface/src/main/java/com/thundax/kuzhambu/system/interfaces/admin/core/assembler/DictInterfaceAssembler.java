package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.system.application.core.command.CreateDictCommand;
import com.thundax.kuzhambu.system.application.core.command.DictSortCommand;
import com.thundax.kuzhambu.system.application.core.command.RemoveDictCommand;
import com.thundax.kuzhambu.system.application.core.query.DictQuery;
import com.thundax.kuzhambu.system.application.core.query.GetDictQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DictIdCodec;
import com.thundax.kuzhambu.system.domain.core.model.entity.Dict;
import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.DictResponse;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DictInterfaceAssembler {
    private DictInterfaceAssembler() {}

    @NonNull
    public static DictResponse toResponse(Dict entity) {
        if (entity == null) {
            return DictResponse.builder().build();
        }
        return DictResponse.builder()
                .id(DictIdCodec.toStringValue(entity.getId()))
                .remarks(entity.getRemarks())
                .label(entity.getLabel())
                .type(entity.getType())
                .value(entity.getValue())
                .build();
    }

    @NonNull
    public static DictId toId(@NonNull DictIdRequest request) {
        return DictIdCodec.toDomain(request.getId());
    }

    @NonNull
    public static DictQuery toQuery(@NonNull DictQueryRequest request) {
        return new DictQuery(
                emptyToNull(request.getType()), emptyToNull(request.getRemarks()), emptyToNull(request.getLabel()));
    }

    @NonNull
    public static DictQuery toQuery(@NonNull DictPageRequest request) {
        return new DictQuery(
                emptyToNull(request.getType()), emptyToNull(request.getRemarks()), emptyToNull(request.getLabel()));
    }

    @NonNull
    public static DictQuery toTypeQuery(String type) {
        return new DictQuery(type, null, null);
    }

    @NonNull
    public static GetDictQuery toGetQuery(DictId id) {
        return new GetDictQuery(id);
    }

    @NonNull
    public static CreateDictCommand toCreateCommand(@NonNull DictSaveRequest request) {
        return new CreateDictCommand(request.getType(), request.getLabel(), request.getValue(), request.getRemarks());
    }

    @NonNull
    public static ChangeDictInfoCommand toChangeInfoCommand(@NonNull DictSaveRequest request) {
        return new ChangeDictInfoCommand(
                DictIdCodec.toDomain(request.getId()),
                request.getType(),
                request.getLabel(),
                request.getValue(),
                request.getRemarks());
    }

    @NonNull
    public static RemoveDictCommand toRemoveCommand(DictId id) {
        return new RemoveDictCommand(id);
    }

    @NonNull
    public static DictSortCommand toSortCommand(@NonNull List<Long> orderedIds) {
        return new DictSortCommand(toIds(orderedIds));
    }

    @NonNull
    public static List<DictId> toIds(@NonNull List<Long> ids) {
        return ids.stream().map(DictIdCodec::toDomain).toList();
    }

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }
}
