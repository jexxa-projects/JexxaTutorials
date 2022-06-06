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

                // Bind a REST adapter to a HelloJexxa object
               .bind(RESTfulRPCAdapter.class).to(HelloJexxa.class)

                //Run the application until CTRL-C is pressed
                // - Open following URL in browser to get greetings: http://localhost:7500/HelloJexxa/greetings
                // - You can also use curl: `curl -X GET http://localhost:7500/HelloJexxa/greetings`
                .run();
    }
}
