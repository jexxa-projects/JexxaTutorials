package io.jexxa.tutorials.bookstore;


import io.jexxa.core.JexxaMain;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.application.port.outbound.DomainEventDispatcher;
import io.jexxa.tutorials.bookstore.application.service.BookStoreService;
import io.jexxa.tutorials.bookstore.application.port.outbound.IntegrationEventSender;
import io.jexxa.tutorials.bookstore.infrastructure.bootstrap.ReferenceLibrary;


public final class BookStore
{
    public static void main()
    {
        var jexxaMain = new JexxaMain(BookStore.class);
        var domainEventDispatcher = jexxaMain.getInstanceOfPort(DomainEventDispatcher.class);

        jexxaMain
                .bootstrap(ReferenceLibrary.class).and()       // Bootstrap latest books via ReferenceLibrary

                // Forward domain events to the integration messaging adapter.
                // Note: Production systems may require explicit event selection,
                // mapping, versioning, and reliable delivery. For simplicity, we forward all DomainEvents as IntegrationEvents
                .bootstrap(IntegrationEventSender.class).with(sender -> domainEventDispatcher.subscribe(sender::publish))

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)        // Provide REST access to BookStoreService
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext()) // Provide REST access to BoundedContext

                .run(); // Finally, run the application
    }

    private BookStore()
    {
        //Private constructor since we only offer main
    }
}
