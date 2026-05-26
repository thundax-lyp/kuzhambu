package com.thundax.kuzhambu.common.mq.configure;

import com.thundax.kuzhambu.common.mq.KuzhambuMqType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kuzhambu.mq")
public class KuzhambuMqProperties {

    private boolean enabled = true;
    private KuzhambuMqType type = KuzhambuMqType.RABBITMQ;
}
