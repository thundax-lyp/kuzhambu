package com.thundax.kuzhambu.operations.interfaces.admin.restore.configure;

import com.thundax.kuzhambu.common.web.restore.RestoreWriteBlockState;
import com.thundax.kuzhambu.operations.application.restore.support.OperationsRestoreWriteBlocker;
import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import java.util.Date;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OperationsRestoreWriteBlockConfiguration {

    @Bean
    @Primary
    public OperationsRestoreWriteBlocker webOperationsRestoreWriteBlocker(RestoreWriteBlockState state) {
        return new WebOperationsRestoreWriteBlocker(state);
    }

    static final class WebOperationsRestoreWriteBlocker extends OperationsRestoreWriteBlocker {

        private final RestoreWriteBlockState state;

        WebOperationsRestoreWriteBlocker(RestoreWriteBlockState state) {
            this.state = state;
        }

        @Override
        public Date enable(RestoreId restoreId) {
            Date enabledAt = super.enable(restoreId);
            state.enable("operations restore " + restoreId.value());
            return enabledAt;
        }

        @Override
        public Date disable(RestoreId restoreId) {
            Date disabledAt = super.disable(restoreId);
            state.disable();
            return disabledAt;
        }
    }
}
