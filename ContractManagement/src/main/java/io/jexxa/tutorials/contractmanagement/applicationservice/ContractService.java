package io.jexxa.tutorials.contractmanagement.applicationservice;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.contractmanagement.domain.contract.Contract;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractSigned;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractNumber;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractRepository;
import io.jexxa.tutorials.contractmanagement.domainservice.DomainEventStore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static io.jexxa.tutorials.contractmanagement.domain.contract.Contract.newContract;

@SuppressWarnings("unused")
@ApplicationService
public class ContractService
{
    private final ContractRepository contractRepository;
    private final DomainEventStore domainEventStore;

    public ContractService(ContractRepository contractRepository, DomainEventStore domainEventStore)
    {
        this.contractRepository = contractRepository;
        this.domainEventStore = domainEventStore;
    }

    public ContractNumber createNewContract(String advisor)
    {
        var newContract = newContract(getNextContractNumber(), advisor);
        contractRepository.add(newContract);
        return newContract.getContractNumber();
    }

    public void signContract( ContractNumber contractNumber )
    {
        var contract = contractRepository.get(contractNumber);
        contract.sign();
        contractRepository.update(contract);
    }

    public List<ContractNumber> getUnsignedContracts()
    {
        return contractRepository
                .getUnsignedContracts()
                .stream()
                .map(Contract::getContractNumber)
                .toList();
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

    public List<ContractNumber> getContractsByAdvisor(String advisor)
    {
        return contractRepository
                .getByAdvisor(advisor)
                .stream()
                .map(Contract::getContractNumber)
                .toList();
    }

    private ContractNumber getNextContractNumber()
    {
        return contractRepository
                .getHighestContractNumber()
                .map(contract -> new ContractNumber(contract.getContractNumber().value() + 1))
                .orElseGet(() -> new ContractNumber(1));
    }
}
