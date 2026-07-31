package com.thundax.kuzhambu.classics.infra.content.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baomidou.mybatisplus.annotation.TableField;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentExportJobDO;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ClassicsContentExportJobMappingTest {

    @Test
    void visibilityRiskShouldUseLifecycleRiskColumn() throws Exception {
        Field field = ClassicsContentExportJobDO.class.getDeclaredField("visibilityRiskStatus");

        assertEquals(
                "lifecycle_risk_status", field.getAnnotation(TableField.class).value());
    }
}
