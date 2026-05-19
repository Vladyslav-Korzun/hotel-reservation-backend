package com.hotel.management.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.hotel.management");

    @Test
    void domain_module_must_not_depend_on_frameworks_or_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.hotel.management.domain..",
                        "com.hotel.management.domain.service..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "com.hotel.management.api..",
                        "com.hotel.management.controller..",
                        "com.hotel.management.mapper..",
                        "com.hotel.management.security..",
                        "com.hotel.management.jpa..",
                        "com.hotel.management")
                .because("the domain module must stay independent from REST, persistence and runtime technology");

        rule.check(CLASSES);
    }

    @Test
    void inbound_adapter_must_not_depend_on_outbound_adapter_or_runtime() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.hotel.management.controller..",
                        "com.hotel.management.mapper..",
                        "com.hotel.management.security..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.hotel.management.jpa..",
                        "com.hotel.management")
                .because("the REST adapter must call facades and must not reach into JPA adapters or runtime wiring");

        rule.check(CLASSES);
    }

    @Test
    void outbound_adapter_must_not_depend_on_inbound_api_or_runtime() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.hotel.management.jpa..",
                        "com.hotel.management.integration..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.hotel.management.api..",
                        "com.hotel.management.controller..",
                        "com.hotel.management.mapper..",
                        "com.hotel.management.security..",
                        "com.hotel.management")
                .because("the JPA adapter must implement domain ports and must not know REST, DTOs, security or runtime wiring");

        rule.check(CLASSES);
    }

    @Test
    void controllers_must_be_explicit_rest_entry_points_or_exception_handlers() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.hotel.management.controller..")
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(RestControllerAdvice.class)
                .because("the controller package should contain only REST entry points and centralized error handling");

        rule.check(CLASSES);
    }

    @Test
    void repository_ports_and_adapters_must_follow_hexagonal_roles() {
        ArchRule repositoryPorts = classes()
                .that().resideInAnyPackage(
                        "com.hotel.management.domain..",
                        "com.hotel.management.domain.service..")
                .and().haveSimpleNameEndingWith("Repository")
                .should().beInterfaces()
                .because("repository classes in the domain module are ports");

        ArchRule repositoryAdapters = classes()
                .that().resideInAPackage("com.hotel.management.jpa..")
                .and().haveSimpleNameEndingWith("RepositoryAdapter")
                .should().beAnnotatedWith(Component.class)
                .andShould(implementDomainRepositoryPort())
                .because("JPA repository adapters must be Spring components implementing domain repository ports");

        repositoryPorts.check(CLASSES);
        repositoryAdapters.check(CLASSES);
    }

    @Test
    void spring_data_repositories_must_stay_internal_to_outbound_adapter() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.hotel.management.jpa..")
                .and().haveSimpleNameEndingWith("SpringDataRepository")
                .should().beInterfaces()
                .andShould().notBePublic()
                .because("Spring Data repositories are an internal detail of the outbound adapter");

        rule.check(CLASSES);
    }

    @Test
    void runtime_root_package_should_only_contain_bootstrap_configuration_and_transaction_wrappers() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.hotel.management")
                .should().beAnnotatedWith(Configuration.class)
                .orShould().beAnnotatedWith(SpringBootApplication.class)
                .orShould().haveSimpleNameStartingWith("Transactional")
                .because("runtime package should contain only application bootstrap, bean wiring and transaction decorators");

        rule.check(CLASSES);
    }

    private static ArchCondition<JavaClass> implementDomainRepositoryPort() {
        return new ArchCondition<>("implement a domain repository port") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                Set<String> repositoryInterfaces = item.getAllRawInterfaces().stream()
                        .map(JavaClass::getFullName)
                        .filter(name -> name.startsWith("com.hotel.management.domain."))
                        .filter(name -> name.endsWith("Repository"))
                        .collect(Collectors.toSet());

                boolean satisfied = !repositoryInterfaces.isEmpty();
                String message = item.getName() + (satisfied
                        ? " implements domain repository port " + repositoryInterfaces
                        : " does not implement any domain repository port");
                events.add(new SimpleConditionEvent(item, satisfied, message));
            }
        };
    }
}
