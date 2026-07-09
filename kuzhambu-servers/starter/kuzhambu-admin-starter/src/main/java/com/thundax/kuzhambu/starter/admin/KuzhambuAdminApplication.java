package com.thundax.kuzhambu.starter.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
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
    "com.thundax.kuzhambu.ai.infra.batch.persistence.mapper",
    "com.thundax.kuzhambu.ai.infra.capability.persistence.mapper",
    "com.thundax.kuzhambu.ai.infra.invocation.persistence.mapper",
    "com.thundax.kuzhambu.ai.infra.model.persistence.mapper",
    "com.thundax.kuzhambu.ai.infra.prompt.persistence.mapper",
    "com.thundax.kuzhambu.ai.infra.refinement.persistence.mapper",
    "com.thundax.kuzhambu.knowledge.infra.graph.persistence.mapper",
    "com.thundax.kuzhambu.knowledge.infra.refinement.persistence.mapper",
    "com.thundax.kuzhambu.knowledge.infra.taxonomy.persistence.mapper",
    "com.thundax.kuzhambu.discovery.infra.qa.persistence.mapper",
    "com.thundax.kuzhambu.discovery.infra.search.persistence.mapper"
})
@SpringBootApplication(
        scanBasePackages = {
            "com.thundax.kuzhambu.starter.admin",
            "com.thundax.kuzhambu.common",
            "com.thundax.kuzhambu.system.application",
            "com.thundax.kuzhambu.system.infra",
            "com.thundax.kuzhambu.system.interfaces.admin",
            "com.thundax.kuzhambu.storage.application",
            "com.thundax.kuzhambu.storage.infra",
            "com.thundax.kuzhambu.storage.interfaces.admin",
            "com.thundax.kuzhambu.classics.application",
            "com.thundax.kuzhambu.classics.infra",
            "com.thundax.kuzhambu.classics.interfaces.admin",
            "com.thundax.kuzhambu.ai.application",
            "com.thundax.kuzhambu.ai.infra",
            "com.thundax.kuzhambu.ai.interfaces.admin",
            "com.thundax.kuzhambu.knowledge.domain",
            "com.thundax.kuzhambu.knowledge.application",
            "com.thundax.kuzhambu.knowledge.infra",
            "com.thundax.kuzhambu.knowledge.interfaces.admin",
            "com.thundax.kuzhambu.discovery.domain",
            "com.thundax.kuzhambu.discovery.application",
            "com.thundax.kuzhambu.discovery.infra",
            "com.thundax.kuzhambu.discovery.interfaces.admin"
        })
public class KuzhambuAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(KuzhambuAdminApplication.class, args);
    }
}
