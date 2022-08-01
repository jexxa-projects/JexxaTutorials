package io.jexxa.tutorials.bookstore;


import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domainservice.IntegrationEventService;
import io.jexxa.tutorials.bookstore.domainservice.ReferenceLibrary;

public final class BookStore
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                //Get the latest books when starting the application
                .bootstrap(IntegrationEventService.class).with(IntegrationEventService::init)
                .bootstrap(ReferenceLibrary.class).with(ReferenceLibrary::addLatestBooks)

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private BookStore()
    {
        //Private constructor since we only offer main
    }
}
