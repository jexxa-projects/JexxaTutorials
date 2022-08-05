package io.jexxa.tutorials.contractmanagement.domain.contract;

import io.jexxa.addend.applicationcore.DomainEvent;

import java.time.Instant;

@DomainEvent
public record ContractSigned(ContractNumber contractNumber, Instant signatureDate)
{
}
