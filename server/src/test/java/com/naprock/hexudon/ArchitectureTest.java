package com.naprock.hexudon;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    @Test
    void domainShouldNotDependOnOuterLayers() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.naprock.hexudon");

        ArchRuleDefinition.noClasses()
                .that().resideInAnyPackage("com.naprock.hexudon.domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.naprock.hexudon.application..",
                        "com.naprock.hexudon.adapter..",
                        "com.naprock.hexudon.infrastructure.."
                )
                .check(importedClasses);
    }

    @Test
    void applicationShouldNotDependOnOuterLayers() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.naprock.hexudon");

        ArchRuleDefinition.noClasses()
                .that().resideInAnyPackage("com.naprock.hexudon.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.naprock.hexudon.adapter..",
                        "com.naprock.hexudon.infrastructure.."
                )
                .check(importedClasses);
    }
}
