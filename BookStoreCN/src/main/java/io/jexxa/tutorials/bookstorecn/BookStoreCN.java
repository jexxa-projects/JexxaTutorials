package io.jexxa.tutorials.bookstorecn;


import io.jexxa.adapters.esp.drivingadapter.KafkaAdapter;
import io.jexxa.adapters.rest.RESTfulRPCAdapter;
import io.jexxa.core.JexxaMain;

import io.jexxa.tutorials.bookstorecn.application.ports.IntegrationEventSender;
import io.jexxa.tutorials.bookstorecn.application.services.BookStoreService;
import io.jexxa.tutorials.bookstorecn.domain.book.BookEvent;
import io.jexxa.tutorials.bookstorecn.infrastructure.bootstrap.ReferenceLibrary;
import io.jexxa.tutorials.bookstorecn.infrastructure.drivingadapter.eventstream.BookSoldOutListener;


public final class BookStoreCN
{
    static void main()
    {
        var jexxaMain = new JexxaMain(BookStoreCN.class);

        jexxaMain
                .bootstrap(ReferenceLibrary.class).and()       // Bootstrap the latest books via ReferenceLibrary
                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)        // Provide REST access to BookStoreService
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext()) // Provide REST access to BoundedContext
                .bind(KafkaAdapter.class).to(BookSoldOutListener.class)
                .run(); // Finally, run the application
    }


    private BookStoreCN()
    {
        //Private constructor since we only offer main
    }
}
