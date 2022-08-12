package io.jexxa.tutorials.bookstore.architecture;

import io.jexxa.tutorials.bookstore.BookStore;
import org.junit.jupiter.api.Test;

import static io.jexxa.jexxatest.architecture.ArchitectureRules.aggregateRules;
import static io.jexxa.jexxatest.architecture.ArchitectureRules.patternLanguage;
import static io.jexxa.jexxatest.architecture.ArchitectureRules.portsAndAdapters;

class ArchitectureTest {

    @Test
    void validatePortsAndAdapters()
    {
        portsAndAdapters(BookStore.class)
                .addDrivenAdapterPackage("persistence")
                .addDrivenAdapterPackage("messaging")
                .validate();
    }

    @Test
    void validatePatternLanguage()
    {
        patternLanguage(BookStore.class)
                .validate();
    }

    @Test
    void validateAggregateRules()
    {
        aggregateRules(BookStore.class)
                .validate();
    }
}