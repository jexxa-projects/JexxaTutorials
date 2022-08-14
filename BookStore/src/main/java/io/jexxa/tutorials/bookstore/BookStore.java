package io.jexxa.tutorials.bookstore;


import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;

public final class BookStore
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                // Bootstrap all classes annotated with @DomainService. In this application this causes to get
                // the latest books via ReferenceLibrary and forward DomainEvents to a message bus via DomainEventService
                .bootstrapAnnotation(DomainService.class)

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private BookStore()
    {
        //Private constructor since we only offer main
    }
}
