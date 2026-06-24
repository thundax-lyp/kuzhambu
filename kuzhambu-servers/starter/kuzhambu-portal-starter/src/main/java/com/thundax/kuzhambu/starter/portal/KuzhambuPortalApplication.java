package com.thundax.kuzhambu.starter.portal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@MapperScan({
    "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
    "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
    "com.thundax.kuzhambu.system.infra.audit.persistence.mapper",
    "com.thundax.kuzhambu.storage.infra.object.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.content.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper",
    "com.thundax.kuzhambu.discovery.infra.search.persistence.mapper"
})
@SpringBootApplication(
        exclude = SecurityAutoConfiguration.class,
        scanBasePackages = {
            "com.thundax.kuzhambu.common",
            "com.thundax.kuzhambu.system.application",
            "com.thundax.kuzhambu.system.infra",
            "com.thundax.kuzhambu.system.interfaces.portal",
            "com.thundax.kuzhambu.storage.application",
            "com.thundax.kuzhambu.storage.infra",
            "com.thundax.kuzhambu.classics.application",
            "com.thundax.kuzhambu.classics.infra",
            "com.thundax.kuzhambu.classics.interfaces.portal",
            "com.thundax.kuzhambu.discovery.application",
            "com.thundax.kuzhambu.discovery.infra",
            "com.thundax.kuzhambu.discovery.interfaces.portal"
        })
public class KuzhambuPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuzhambuPortalApplication.class, args);
    }
}
