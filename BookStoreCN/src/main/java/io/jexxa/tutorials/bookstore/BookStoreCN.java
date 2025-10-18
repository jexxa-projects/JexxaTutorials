package io.jexxa.tutorials.bookstore;


import io.jexxa.core.JexxaMain;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domainservice.IntegrationEventSender;
import io.jexxa.tutorials.bookstore.domainservice.ReferenceLibrary;

import static io.jexxa.tutorials.bookstore.domain.DomainEventPublisher.subscribe;

public final class BookStoreCN
{
    static void main()
    {
        var jexxaMain = new JexxaMain(BookStoreCN.class);

        jexxaMain
                .bootstrap(ReferenceLibrary.class).and()       // Bootstrap the latest books via ReferenceLibrary
                .bootstrap(IntegrationEventSender.class).with(sender -> subscribe(sender::publish)) // publish all DomainEvents as IntegrationEvents for other bounded contexts

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)        // Provide REST access to BookStoreService
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext()) // Provide REST access to BoundedContext

                .run(); // Finally, run the application
    }

    private BookStoreCN()
    {
        //Private constructor since we only offer main
    }
}
