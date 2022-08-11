package io.jexxa.tutorials.contractmanagement.infrastructure.drivenadapter.persistence;

import io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.IObjectStore;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTag;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.tutorials.contractmanagement.domain.contract.Contract;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractNumber;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractRepository;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.ObjectStoreManager.getObjectStore;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTags.booleanTag;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTags.numericTag;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTags.stringTag;
import static io.jexxa.tutorials.contractmanagement.infrastructure.drivenadapter.persistence.ContractRepositoryImpl.ContractSchema.ADVISOR;
import static io.jexxa.tutorials.contractmanagement.infrastructure.drivenadapter.persistence.ContractRepositoryImpl.ContractSchema.CONTRACT_NUMBER;
import static io.jexxa.tutorials.contractmanagement.infrastructure.drivenadapter.persistence.ContractRepositoryImpl.ContractSchema.CONTRACT_SIGNED;

@SuppressWarnings("unused")
public class ContractRepositoryImpl implements ContractRepository
{
    /**
     * Here we define the values to query contracts. Apart from their key, elements should be queried by following information: <br>
     * <ol>
     *    <li>Contract number</li>
     *    <li>Contract signed flag</li>
     *    <li>Advisor of the contract</li>
     * </ol>
     */
    enum ContractSchema implements MetadataSchema
    {
        /**
         * This MetaTag represents the contract number. Since contract number is a  numeric value we use a numberTag. As most
         * predefined {@link MetaTag} class, we just provide an accessor function to get the value from the managed object.
         */
        CONTRACT_NUMBER(numericTag(element -> element.getContractNumber().value())),

        /**
         * This MetaTag represents a boolean if the contract is signed or not.  Here, we use booleanTag together with the
         * corresponding accessor function.
         */
        CONTRACT_SIGNED(booleanTag(Contract::isSigned)),

        /**
         * This MetaTag allows for searching for the advisor of the contract. Here, we use stringTag together with the corresponding
         * accessor function.
         */
        ADVISOR(stringTag(Contract::getAdvisor));

        // The remaining code is always the same for all metadata specifications
        private final MetaTag<Contract, ?, ? > metaTag;

        ContractSchema(MetaTag<Contract,?, ?> metaTag)
        {
            this.metaTag = metaTag;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MetaTag<Contract, ?, ?> getTag()
        {
            return metaTag;
        }
    }


    private final IObjectStore<Contract, ContractNumber, ContractSchema> objectStore;

    public ContractRepositoryImpl(Properties properties)
    {
        this.objectStore = getObjectStore(Contract.class, Contract::getContractNumber, ContractSchema.class, properties);
    }

    @Override
    public void add(Contract contract)
    {
        objectStore.add(contract);
    }

    @Override
    public void update(Contract contract)
    {
        objectStore.update(contract);
    }

    @Override
    public void remove(ContractNumber contractNumber)
    {
        objectStore.remove(contractNumber);
    }


    @Override
    public List<Contract> getByAdvisor(String advisor)
    {
        return objectStore
                .getStringQuery(ADVISOR, String.class)
                .isEqualTo(advisor);
    }

    @Override
    public Contract get(ContractNumber contractNumber)
    {
        return objectStore
                .get(contractNumber)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public List<Contract> getAll()
    {
        return objectStore.get();
    }

    @Override
    public List<Contract> getSignedContracts()
    {
        return objectStore
                .getNumericQuery(CONTRACT_SIGNED, Boolean.class)
                .isEqualTo(true);
    }

    @Override
    public List<Contract> getUnsignedContracts()
    {
        return objectStore
                .getNumericQuery(CONTRACT_SIGNED, Boolean.class)
                .isEqualTo(false);
    }

    @Override
    public Optional<Contract> getHighestContractNumber()
    {
        return objectStore
                .getNumericQuery(CONTRACT_NUMBER, Integer.class)
                .getDescending(1)
                .stream()
                .findFirst();
    }

}
