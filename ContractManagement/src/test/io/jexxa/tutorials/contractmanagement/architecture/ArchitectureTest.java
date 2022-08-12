package io.jexxa.tutorials.contractmanagement.architecture;

import io.jexxa.tutorials.contractmanagement.ContractManagement;
import org.junit.jupiter.api.Test;

import static io.jexxa.jexxatest.architecture.ArchitectureRules.aggregateRules;
import static io.jexxa.jexxatest.architecture.ArchitectureRules.patternLanguage;
import static io.jexxa.jexxatest.architecture.ArchitectureRules.portsAndAdapters;

class ArchitectureTest {

    @Test
    void validatePortsAndAdapters()
    {
        portsAndAdapters(ContractManagement.class)
                .addDrivenAdapterPackage("persistence")
                .validate();
    }

    @Test
    void validatePatternLanguage()
    {
        patternLanguage(ContractManagement.class)
                .validate();
    }

    @Test
    void validateAggregateRules()
    {
        aggregateRules(ContractManagement.class)
                .validate();
    }
}