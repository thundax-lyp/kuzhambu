package com.thundax.kuzhambu.ai.infra.capability.persistence.assembler;

import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.infra.capability.persistence.dataobject.AiActionStatusDO;
import java.util.ArrayList;
import java.util.List;

public final class AiActionStatusPersistenceAssembler {

    private AiActionStatusPersistenceAssembler() {}

    public static AiActionStatusDO toObject(AiActionStatus actionStatus) {
        if (actionStatus == null) {
            return null;
        }
        AiActionStatusDO dataObject = new AiActionStatusDO();
        dataObject.setId(actionStatus.getId());
        dataObject.setActionStatusId(actionStatus.getActionStatusId());
        dataObject.setScope(actionStatus.getScope());
        dataObject.setCapability(actionStatus.getCapability());
        dataObject.setAvailable(actionStatus.isAvailable());
        dataObject.setUnavailableReason(actionStatus.getUnavailableReason());
        dataObject.setCheckedAt(actionStatus.getCheckedAt());
        return dataObject;
    }

    public static AiActionStatus toDomain(AiActionStatusDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new AiActionStatus(
                dataObject.getId(),
                dataObject.getActionStatusId(),
                dataObject.getScope(),
                dataObject.getCapability(),
                Boolean.TRUE.equals(dataObject.getAvailable()),
                dataObject.getUnavailableReason(),
                dataObject.getCheckedAt());
    }

    public static List<AiActionStatus> toDomainList(List<AiActionStatusDO> dataObjects) {
        List<AiActionStatus> statuses = new ArrayList<>();
        if (dataObjects == null) {
            return statuses;
        }
        for (AiActionStatusDO dataObject : dataObjects) {
            statuses.add(toDomain(dataObject));
        }
        return statuses;
    }
}
