package io.jexxa.tutorials.contractmanagement.domain.contract;

import io.jexxa.addend.applicationcore.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository
{
    void add(Contract contract);

    void update(Contract contract);

    void remove(ContractNumber contractNumber);

    List<Contract> getByAdvisor(String advisor);

    Contract get(ContractNumber contractNumber);

    List<Contract> getAll();

    List<Contract> getSignedContracts();

    List<Contract> getUnsignedContracts();

    Optional<Contract> getHighestContractNumber();
}
