package io.jexxa.tutorials.contractmanagement.application.service;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.contractmanagement.application.port.outbound.DomainEventDispatcher;
import io.jexxa.tutorials.contractmanagement.domain.contract.Contract;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractNumber;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractRepository;

import java.util.List;

import static io.jexxa.tutorials.contractmanagement.domain.contract.Contract.newContract;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("unused")
@ApplicationService
public class ContractService
{
    private final ContractRepository contractRepository;
    private final DomainEventDispatcher domainEventDispatcher;


    public ContractService(
            ContractRepository contractRepository,
            DomainEventDispatcher domainEventDispatcher)
    {
        this.contractRepository = requireNonNull(contractRepository);
        this.domainEventDispatcher = requireNonNull(domainEventDispatcher);
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
        var result = contract.sign();
        contractRepository.update(contract);
        domainEventDispatcher.publish(result);
    }

    public List<ContractNumber> getUnsignedContracts()
    {
        return contractRepository
                .getUnsignedContracts()
                .stream()
                .map(Contract::getContractNumber)
                .toList();
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
