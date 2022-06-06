package io.jexxa.tutorials.bookstore;


import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domainservice.ReferenceLibrary;
import io.jexxa.utils.JexxaLogger;

import static io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSenderManager.getDefaultMessageSender;
import static io.jexxa.infrastructure.drivenadapterstrategy.persistence.repository.RepositoryManager.getDefaultRepository;

public final class BookStore
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        JexxaLogger.getLogger(BookStore.class).info("Used Repository    : {}", getDefaultRepository(jexxaMain.getProperties()).getSimpleName());
        JexxaLogger.getLogger(BookStore.class).info("Used MessageSender : {}", getDefaultMessageSender(jexxaMain.getProperties()).getSimpleName());

        jexxaMain
                //Get the latest books when starting the application
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
