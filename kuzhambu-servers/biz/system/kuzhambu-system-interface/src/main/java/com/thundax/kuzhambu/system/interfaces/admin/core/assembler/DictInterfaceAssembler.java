package com.thundax.kuzhambu.system.interfaces.admin.core.assembler;

import com.thundax.kuzhambu.system.application.core.entity.Dict;
import com.thundax.kuzhambu.system.application.core.service.command.ChangeDictInfoCommand;
import com.thundax.kuzhambu.system.application.core.service.command.CreateDictCommand;
import com.thundax.kuzhambu.system.application.core.service.query.DictQuery;
import com.thundax.kuzhambu.system.domain.core.codec.DictIdCodec;
import com.thundax.kuzhambu.system.domain.model.valueobject.DictId;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictIdRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictPageRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictQueryRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.request.DictSaveRequest;
import com.thundax.kuzhambu.system.interfaces.admin.core.controller.response.DictResponse;
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
        DictQuery query = new DictQuery();
        query.setLabel(emptyToNull(request.getLabel()));
        query.setType(emptyToNull(request.getType()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        return query;
    }

    @NonNull
    public static DictQuery toQuery(@NonNull DictPageRequest request) {
        DictQuery query = new DictQuery();
        query.setLabel(emptyToNull(request.getLabel()));
        query.setType(emptyToNull(request.getType()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        return query;
    }

    @NonNull
    public static CreateDictCommand toCreateCommand(@NonNull DictSaveRequest request) {
        CreateDictCommand command = new CreateDictCommand();
        command.setRemarks(request.getRemarks());
        command.setLabel(request.getLabel());
        command.setType(request.getType());
        command.setValue(request.getValue());
        return command;
    }

    @NonNull
    public static ChangeDictInfoCommand toChangeInfoCommand(@NonNull DictSaveRequest request) {
        ChangeDictInfoCommand command = new ChangeDictInfoCommand();
        command.setId(DictIdCodec.toDomain(request.getId()));
        command.setRemarks(request.getRemarks());
        command.setLabel(request.getLabel());
        command.setType(request.getType());
        command.setValue(request.getValue());
        return command;
    }

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }
}
