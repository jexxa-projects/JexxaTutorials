package io.jexxa.tutorials.contractmanagement.domainservice;

import io.jexxa.addend.applicationcore.InfrastructureService;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractSigned;

import java.time.Instant;
import java.util.List;

@InfrastructureService
public interface DomainEventStore
{
    void add(ContractSigned domainEvent);

    List<ContractSigned> get(Instant startTime, Instant endTime);

    List<ContractSigned> get();
}
