package io.jexxa.tutorials.contractmanagement.infrastructure.drivenadapter.persistence;

import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.IObjectStore;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTag;
import io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetadataSchema;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractSigned;
import io.jexxa.tutorials.contractmanagement.domain.contract.ContractNumber;
import io.jexxa.tutorials.contractmanagement.domainservice.DomainEventStore;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.ObjectStoreManager.getObjectStore;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTags.instantTag;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.objectstore.metadata.MetaTags.numericTag;

@SuppressWarnings("unused")
@DrivenAdapter
public class DomainEventStoreImpl implements DomainEventStore
{
    /**
     * Here we define the values to query DomainEvents. The domain events should be queried by following information:
     * <ol>
     *  <li>Contract number</li>
     *  <li>Date of the signature</li>
     * </ol>
     */
    public enum DomainEventSchema implements MetadataSchema
    {
        CONTRACT_NUMBER(numericTag((domainEvent -> domainEvent.contractNumber().value())) ),

        SIGNATURE_DATE(instantTag(ContractSigned::signatureDate));

        // The remaining code is always the same for all metadata specifications
        private final MetaTag<ContractSigned, ?, ? > metaTag;

        DomainEventSchema(MetaTag<ContractSigned,?, ?> metaTag)
        {
            this.metaTag = metaTag;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MetaTag<ContractSigned, ?, ?> getTag()
        {
            return metaTag;
        }
    }

    private final IObjectStore<ContractSigned, ContractNumber, DomainEventSchema> objectStore;


    public DomainEventStoreImpl(Properties properties)
    {
        this.objectStore = getObjectStore(ContractSigned.class, ContractSigned::contractNumber, DomainEventSchema.class, properties);
    }

    @Override
    public void add(ContractSigned domainEvent)
    {
        objectStore.add(domainEvent);
    }

    @Override
    public List<ContractSigned> get(Instant startTime, Instant endTime)
    {
        return objectStore
                .getNumericQuery(DomainEventSchema.SIGNATURE_DATE, Instant.class)
                .getRangeClosed(startTime, endTime);
    }

    @Override
    public List<ContractSigned> get()
    {
        return objectStore.get();
    }
}
