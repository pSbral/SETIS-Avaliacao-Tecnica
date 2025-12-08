package com.psbral.projeto;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;


@AnalyzeClasses(packages = "com.psbral.projeto")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_must_not_access_repository =
            ArchRuleDefinition.noClasses()
                    .that().resideInAPackage("..controllers..")
                    .should().accessClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule controllers_must_not_access_service =
            ArchRuleDefinition.noClasses()
                    .that().resideInAPackage("..controllers..")
                    .should().accessClassesThat().resideInAPackage("..service..");
}
