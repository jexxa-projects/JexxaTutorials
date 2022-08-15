package io.jexxa.tutorials.timeservice.infrastructure.drivingadapter.messaging;

import io.jexxa.infrastructure.drivingadapter.messaging.JMSConfiguration;
import io.jexxa.infrastructure.drivingadapter.messaging.listener.TypedMessageListener;
import io.jexxa.tutorials.timeservice.applicationservice.TimeApplicationService;

import java.time.LocalTime;

import static io.jexxa.infrastructure.drivingadapter.messaging.JMSConfiguration.MessagingType.TOPIC;

@SuppressWarnings("unused")
public final class TimeListener extends TypedMessageListener<LocalTime>
{
    private final TimeApplicationService timeApplicationService;
    private static final String TIME_TOPIC = "TimeService";

    // To implement a so called PortAdapter we need a public constructor which expects a single argument that must be an
    // InboundPort.
    public TimeListener(TimeApplicationService timeApplicationService)
    {
        super(LocalTime.class);
        this.timeApplicationService = timeApplicationService;
    }

    @Override
    // The JMS specific configuration is defined via annotation.
    @JMSConfiguration(destination = TIME_TOPIC,  messagingType = TOPIC)
    public void onMessage(LocalTime localTime)
    {
        // Forward this information to corresponding application service.
        timeApplicationService.displayPublishedTime(localTime);
    }
}
