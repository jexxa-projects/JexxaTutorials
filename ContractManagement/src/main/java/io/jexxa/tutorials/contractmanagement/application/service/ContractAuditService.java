package io.jexxa.tutorials.contractmanagement.application.service;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.contractmanagement.application.port.outbound.DomainEventDispatcher;
import io.jexxa.tutorials.contractmanagement.application.port.outbound.DomainEventStore;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractSigned;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static java.util.Objects.requireNonNull;

@ApplicationService
@SuppressWarnings("unused")
public class ContractAuditService
{
    private final DomainEventStore domainEventStore;
    private final DomainEventDispatcher domainEventDispatcher;

    public ContractAuditService(
            DomainEventDispatcher domainEventDispatcher,
            DomainEventStore domainEventStore)
    {
        this.domainEventStore = requireNonNull(domainEventStore);
        this.domainEventDispatcher = requireNonNull(domainEventDispatcher);
    }

    public void handle(ContractSigned event)
    {
        domainEventStore.add(event);
    }

    public List<ContractSigned> getAllSignedContracts()
    {
        return domainEventStore.get();
    }

    public List<ContractSigned> getSignedContracts(int month, int year)
    {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        return domainEventStore.get(
                startDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                endDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC));
    }

    public void initialize() {
        domainEventDispatcher.subscribe(ContractSigned.class, this::handle);
    }
}