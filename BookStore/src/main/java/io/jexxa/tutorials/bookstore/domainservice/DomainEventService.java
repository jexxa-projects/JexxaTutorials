package io.jexxa.tutorials.bookstore.domainservice;

import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.tutorials.bookstore.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstore.domain.common.DomainEventPublisher;
import io.jexxa.tutorials.bookstore.domain.common.DomainEventSubscriber;

@DomainService
public class DomainEventService implements DomainEventSubscriber<BookSoldOut> {

    private final IDomainEventPublisher iDomainEventPublisher;

    public DomainEventService(IDomainEventPublisher iDomainEventPublisher)
    {
        this.iDomainEventPublisher = iDomainEventPublisher;
    }

    public void init()
    {
        DomainEventPublisher.instance().subscribe(this);
    }

    @Override
    public void handleEvent(Object aDomainEvent)
    {
        iDomainEventPublisher.publish(aDomainEvent);
    }

    @Override
    public Class<BookSoldOut> subscribedToEventType() {
        return BookSoldOut.class;
    }
}
