package com.thundax.kuzhambu.starter.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({
    "com.thundax.kuzhambu.system.infra.core.mapper",
    "com.thundax.kuzhambu.system.infra.auth.mapper",
    "com.thundax.kuzhambu.system.infra.audit.mapper",
    "com.thundax.kuzhambu.storage.infra.mapper"
})
@SpringBootApplication(
        scanBasePackages = {
            "com.thundax.kuzhambu.common",
            "com.thundax.kuzhambu.system.application",
            "com.thundax.kuzhambu.system.infra",
            "com.thundax.kuzhambu.system.interfaces.admin",
            "com.thundax.kuzhambu.storage.application",
            "com.thundax.kuzhambu.storage.infra"
        })
public class KuzhambuAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuzhambuAdminApplication.class, args);
    }
}
