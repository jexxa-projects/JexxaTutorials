package io.jexxa.tutorials;

import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;

public final class HelloJexxa
{
    @SuppressWarnings({"java:S3400", "unused"})
    public String greetings()
    {
        return "Hello Jexxa";
    }

    public static void main(String[] args)
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(HelloJexxa.class);

        jexxaMain
                // Bind a REST adapter to class HelloJexxa to expose its methods
                // To get greetings open: http://localhost:7500/HelloJexxa/greetings
                .bind(RESTfulRPCAdapter.class).to(HelloJexxa.class)

                // Run Jexxa and all bindings until Ctrl-C is pressed
                .run();
    }
}
