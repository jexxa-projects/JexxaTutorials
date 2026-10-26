package io.jexxa.tutorials;

import io.jexxa.core.JexxaMain;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;

public final class HelloJexxa
{
    static void main()
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(HelloJexxa.class);

        jexxaMain
                // Bind a REST adapter to expose parts of the application
                .bind(RESTfulRPCAdapter.class).to(GreetingService.class)          // Get greetings: http://localhost:7501/GreetingService/greetings
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())  // Get stats: http://localhost:7501/BoundedContext/isRunning

                // Run your application until Ctrl-C is pressed
                .run();
    }
}
