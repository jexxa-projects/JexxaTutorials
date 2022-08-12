package io.jexxa.tutorials.bookstore;


import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domainservice.DomainEventService;
import io.jexxa.tutorials.bookstore.domainservice.ReferenceLibrary;

public final class BookStore
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                .bootstrap(DomainEventService.class).with(DomainEventService::registerListener) //Publish all domain events to a message bus
                .bootstrap(ReferenceLibrary.class).with(ReferenceLibrary::addLatestBooks)          //Get the latest books when starting the application

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private BookStore()
    {
        //Private constructor since we only offer main
    }
}
