package io.jexxa.tutorials.timeservice;


import io.jexxa.adapters.jms.drivingadapter.JMSAdapter;
import io.jexxa.adapters.rest.RESTfulRPCAdapter;
import io.jexxa.core.JexxaMain;
import io.jexxa.tutorials.timeservice.application.service.TimeApplicationService;
import io.jexxa.tutorials.timeservice.infrastructure.drivingadapter.messaging.TimeListener;

public final class TimeService
{
    static void main()
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(TimeService.class);

        jexxaMain
                // Bind RESTfulRPCAdapter and JMXAdapter to TimeService class so that we can invoke its method
                .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                // Bind the JMSAdapter to our message listener
                .bind(JMSAdapter.class).to(TimeListener.class)

                .run();
    }
}
