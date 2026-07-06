package com.thundax.kuzhambu.common.web.restore;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;

public class RestoreWriteBlockState {

    private final AtomicReference<String> reason = new AtomicReference<>();

    public void enable(String reason) {
        this.reason.set(StringUtils.defaultIfBlank(reason, "restore"));
    }

    public void disable() {
        reason.set(null);
    }

    public boolean isBlocked() {
        return reason.get() != null;
    }

    public String reason() {
        return reason.get();
    }
}
