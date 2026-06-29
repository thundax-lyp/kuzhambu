package com.thundax.kuzhambu.operations.application.restore.query;

import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsRestoreDetailQuery {
    private RestoreId restoreId;
}
