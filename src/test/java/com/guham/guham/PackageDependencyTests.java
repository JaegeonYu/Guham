package com.guham.guham;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.syntax.GivenSlices;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public class PackageDependencyTests {

    private static final String TEAM = "..modules.team..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    JavaClasses jc = new ClassFileImporter()
            .importPackages("com.guham.guham..");

    @Test
    public void teamPackageRuleCheck(){
        ArchRule teamPackageRule = classes().that().resideInAPackage(TEAM)
                .should().onlyBeAccessed()
                .byClassesThat().resideInAnyPackage(TEAM, EVENT);
        teamPackageRule.check(jc);
    }

    @Test
    public void eventPackageRuleCheck(){
        ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
                .should().accessClassesThat()
                .resideInAnyPackage(TEAM,ACCOUNT, EVENT);
        eventPackageRule.check(jc);
    }

    @Test
    public void accountPackageRuleCheck(){
        ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
                .should().accessClassesThat()
                .resideInAnyPackage(ACCOUNT, ZONE, TAG);
        accountPackageRule.check(jc);
    }

    @Test
    public void test(){
        ArchRule matching = slices().matching("com.guham.guham.modules.(*)..")
                .should().beFreeOfCycles();
        matching.check(jc);
    }

}
