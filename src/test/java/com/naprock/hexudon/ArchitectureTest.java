//package com.naprock.hexudon;
//
//import com.tngtech.archunit.core.domain.JavaClasses;
//import com.tngtech.archunit.core.importer.ClassFileImporter;
//import com.tngtech.archunit.core.importer.ImportOption;
//import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
//import org.junit.jupiter.api.Test;
//
//public class ArchitectureTest {
//
//    @Test
//    void domainCoreShouldNotDependOnOuterPackages() {
//        JavaClasses importedClasses = new ClassFileImporter()
//                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
//                .importPackages("com.naprock.hexudon");
//
//        ArchRuleDefinition.noClasses()
//                .that().resideInAnyPackage("com.naprock.hexudon.model..", "com.naprock.hexudon.engine..")
//                .should().dependOnClassesThat().resideInAnyPackage(
//                        "com.naprock.hexudon.dto..",
//                        "com.naprock.hexudon.controller..",
//                        "com.naprock.hexudon.manager.."
//                    )
//                .check(importedClasses);
//    }
//}
