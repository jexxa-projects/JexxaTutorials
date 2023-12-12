package io.jexxa.tutorials.timeservice.infrastructure.drivingadapter.messaging;


import io.jexxa.common.drivingadapter.messaging.jms.JMSConfiguration;
import io.jexxa.common.drivingadapter.messaging.jms.listener.TypedMessageListener;
import io.jexxa.tutorials.timeservice.applicationservice.TimeApplicationService;

import java.time.LocalTime;

import static io.jexxa.common.drivingadapter.messaging.jms.JMSConfiguration.MessagingType.TOPIC;


/**
 * 1. Within the constructor, we define our class from the application core that will be called. Jexxa automatically
 * injects this object when creating the port adapter. By convention, this is the only object defined in the
 * constructor.
 * <p>
 * 2. In the case of JMS, we have to implement the JMS specific `MessageListener` interface. To facilitate this, Jexxa offers
 * convenience classes such as TypedMessageListener which perform JSON deserialization into a defined type.
 * <p>
 * 3. The JMS specific connection information is defined as annotation at the onMessage method.
 * <p>
 * 4. Finally, the implementation of this method just forwards received data to the application service.
 */
@SuppressWarnings("unused")
public final class TimeListener extends TypedMessageListener<LocalTime>
{
    private static final String TIME_TOPIC = "TimeService";
    private final TimeApplicationService timeApplicationService;

    public TimeListener(TimeApplicationService timeApplicationService)
    {
        super(LocalTime.class);
        this.timeApplicationService = timeApplicationService;
    }

    @Override
    @JMSConfiguration(destination = TIME_TOPIC, messagingType = TOPIC)
    public void onMessage(LocalTime localTime)
    {
        // Forward this information to the corresponding application service.
        timeApplicationService.showReceivedTime(localTime);
    }
}
