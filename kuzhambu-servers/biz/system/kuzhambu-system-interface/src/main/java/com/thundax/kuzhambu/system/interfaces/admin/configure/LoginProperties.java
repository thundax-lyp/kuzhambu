package com.thundax.kuzhambu.system.interfaces.admin.configure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.login")
public class LoginProperties {

    private boolean enable;
    private int maxFailCount;
    private int expire;
    private int lockTime;

    public boolean getEnable() {
        return enable;
    }

    public String getHours() {
        return hours(expire);
    }

    public String getLockHours() {
        return hours(lockTime);
    }

    private static String hours(int seconds) {
        int scale = seconds % 3600 == 0 ? 0 : 1;
        return new BigDecimal(seconds)
                .divide(new BigDecimal(3600), scale, RoundingMode.HALF_UP)
                .toString();
    }
}
