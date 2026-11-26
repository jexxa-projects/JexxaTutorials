package io.jexxa.tutorials.bookstore;


import io.jexxa.adapters.rest.RESTfulRPCAdapter;
import io.jexxa.core.JexxaMain;
import io.jexxa.metrics.MetricsCollector;
import io.jexxa.tutorials.bookstore.application.service.BookStoreService;


public final class BookStore
{
    static void main()
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                .registerService( MetricsCollector::micrometerCollector )

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)        // Provide REST access to BookStoreService
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext()) // Provide REST access to BoundedContext

                .run(); // Finally, run the application
    }
}
