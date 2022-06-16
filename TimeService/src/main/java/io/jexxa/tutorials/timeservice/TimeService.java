package io.jexxa.tutorials.timeservice;

import io.jexxa.core.JexxaMain;
import io.jexxa.infrastructure.drivingadapter.messaging.JMSAdapter;
import io.jexxa.infrastructure.drivingadapter.rest.RESTfulRPCAdapter;
import io.jexxa.tutorials.timeservice.applicationservice.TimeApplicationService;
import io.jexxa.tutorials.timeservice.infrastructure.drivingadapter.messaging.PublishTimeListener;

public final class TimeService
{
    public static void main(String[] args)
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(TimeService.class);

        jexxaMain
                // Bind RESTfulRPCAdapter and JMXAdapter to TimeService class so that we can invoke its method
                .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)

                // Conditional bind is only executed if given expression evaluates to true
                .bind( JMSAdapter.class).to(PublishTimeListener.class)

                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

    private TimeService()
    {
        //Private constructor since we only offer main
    }
}
