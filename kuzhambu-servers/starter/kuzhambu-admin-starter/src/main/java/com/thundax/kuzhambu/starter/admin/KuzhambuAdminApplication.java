package com.thundax.kuzhambu.starter.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({
    "com.thundax.kuzhambu.system.infra.core.persistence.mapper",
    "com.thundax.kuzhambu.system.infra.auth.persistence.mapper",
    "com.thundax.kuzhambu.system.infra.audit.persistence.mapper",
    "com.thundax.kuzhambu.storage.infra.object.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.wangqi.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.sancai.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.content.persistence.mapper",
    "com.thundax.kuzhambu.classics.infra.sharing.persistence.mapper"
})
@SpringBootApplication(
        scanBasePackages = {
            "com.thundax.kuzhambu.common",
            "com.thundax.kuzhambu.system.application",
            "com.thundax.kuzhambu.system.infra",
            "com.thundax.kuzhambu.system.interfaces.admin",
            "com.thundax.kuzhambu.storage.application",
            "com.thundax.kuzhambu.storage.infra",
            "com.thundax.kuzhambu.storage.interfaces.admin",
            "com.thundax.kuzhambu.classics.application",
            "com.thundax.kuzhambu.classics.infra",
            "com.thundax.kuzhambu.classics.interfaces.admin"
        })
public class KuzhambuAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuzhambuAdminApplication.class, args);
    }
}
