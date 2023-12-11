package io.jexxa.tutorials.timeservice;

import io.jexxa.core.JexxaMain;

import io.jexxa.common.drivingadapter.messaging.jms.JMSAdapter;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.timeservice.applicationservice.TimeApplicationService;
import io.jexxa.tutorials.timeservice.infrastructure.drivingadapter.messaging.TimeListener;

public final class TimeService
{
    public static void main(String[] args)
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(TimeService.class);

        jexxaMain
                // Bind RESTfulRPCAdapter and JMXAdapter to TimeService class so that we can invoke its method
                .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                // Bind the JMSAdapter to our message listener
                .bind(JMSAdapter.class).to(TimeListener.class)
                .bind(JMSAdapter.class).to(TimeListener.class)

                .run();
    }

    private TimeService()
    {
        //Private constructor since we only offer main
    }
}
