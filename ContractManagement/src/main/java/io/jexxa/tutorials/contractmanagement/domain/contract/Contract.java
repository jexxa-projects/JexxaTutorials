package io.jexxa.tutorials.contractmanagement.domain.contract;

import io.jexxa.addend.applicationcore.Aggregate;
import io.jexxa.addend.applicationcore.AggregateFactory;
import io.jexxa.addend.applicationcore.AggregateID;

import java.time.Instant;
import java.util.Objects;

import static io.jexxa.tutorials.contractmanagement.domain.DomainEventPublisher.publish;

@Aggregate
public class Contract
{
    private final ContractNumber contractNumber;
    private String advisor;
    private boolean isSigned;

    private Contract(ContractNumber contractNumber, String advisor)
    {
        this.contractNumber = Objects.requireNonNull( contractNumber );
        this.advisor = Objects.requireNonNull( advisor );
        this.isSigned = false;
    }

    @AggregateID
    public ContractNumber getContractNumber()
    {
        return contractNumber;
    }

    public String getAdvisor()
    {
        return advisor;
    }

    @SuppressWarnings("unused")
    public void setAdvisor(String advisor)
    {
        this.advisor = advisor;
    }

    public void sign()
    {
        isSigned = true;
        publish(new ContractSigned(contractNumber, Instant.now()));
    }

    public boolean isSigned()
    {
        return isSigned;
    }

    @AggregateFactory(Contract.class)
    public static Contract newContract(ContractNumber contractNumber, String advisor)
    {
        return new Contract(contractNumber, advisor);
    }
}
