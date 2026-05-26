package com.thundax.kuzhambu.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

public abstract class AbstractArchitectureTest {

    protected JavaClasses importPackages(String... packages) {
        return new ClassFileImporter().importPackages(packages);
    }
}
