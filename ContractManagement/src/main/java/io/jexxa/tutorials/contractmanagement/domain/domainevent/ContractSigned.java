package io.jexxa.tutorials.contractmanagement.domain.domainevent;

import io.jexxa.addend.applicationcore.DomainEvent;
import io.jexxa.tutorials.contractmanagement.domain.valueobject.ContractNumber;

import java.time.Instant;

@DomainEvent
public record ContractSigned(ContractNumber contractNumber, Instant signatureDate)
{
}
