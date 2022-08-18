package io.jexxa.tutorials;

import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;

public final class HelloJexxa
{
    @SuppressWarnings({"java:S3400", "unused"})
    // Our business logic ;-)
    public String greetings()
    {
        return "Hello Jexxa";
    }

    public static void main(String[] args)
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(HelloJexxa.class);

        jexxaMain
                // Bind a REST adapter to expose parts of the application
                .bind(RESTfulRPCAdapter.class).to(HelloJexxa.class)               // Get greetings: http://localhost:7501/HelloJexxa/greetings
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())  // Get stats: http://localhost:7501/BoundedContext/isRunning

                // Run your application until Ctrl-C is pressed
                .run();
    }
}
