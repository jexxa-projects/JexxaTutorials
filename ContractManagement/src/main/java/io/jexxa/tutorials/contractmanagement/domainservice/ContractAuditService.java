package io.jexxa.tutorials.contractmanagement.domainservice;

import io.jexxa.addend.applicationcore.DomainEventHandler;
import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractSigned;

import static io.jexxa.tutorials.contractmanagement.domain.DomainEventPublisher.subscribe;

@DomainService
@SuppressWarnings("unused")
public class ContractAuditService
{
    private final DomainEventStore domainEventStore;

    public ContractAuditService(DomainEventStore domainEventStore)
    {
        this.domainEventStore = domainEventStore;
        subscribe(ContractSigned.class, this::storeAuditEvent);
    }

    @DomainEventHandler
    public void storeAuditEvent(ContractSigned contractSigned)
    {
        domainEventStore.add(contractSigned);
    }
}
