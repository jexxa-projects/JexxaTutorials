package io.jexxa.tutorials.timeservice.infrastructure.drivenadapter.messaging;

import io.jexxa.infrastructure.messaging.MessageSender;
import io.jexxa.tutorials.timeservice.domainservice.TimePublisher;

import java.time.LocalTime;
import java.util.Properties;

import static io.jexxa.infrastructure.MessageSenderManager.getMessageSender;


@SuppressWarnings("unused")
public class TimePublisherImpl implements TimePublisher
{
    public static final String TIME_SERVICE_TOPIC = "TimeService";

    private final MessageSender messageSender;

    // `getMessageSender()` requires a Properties object including all required config information. Therefore, we must
    // use a constructor expecting `Properties`, so that Jexxa can hand in all defined properties (e.g., from `jexxa-application.properties`).
    public TimePublisherImpl(Properties properties)
    {
        //Request a default message sender for the implemented interface TimePublisher
        this.messageSender = getMessageSender(TimePublisher.class, properties);
    }

    @Override
    public void publish(LocalTime localTime)
    {
        // For most integrated standard APIs, Jexxa provides a fluent API to improve readability
        // and to emphasize the purpose of the code
        messageSender
                .send(localTime)
                .toTopic(TIME_SERVICE_TOPIC)
                .addHeader("Type", localTime.getClass().getSimpleName())
                .asJson();
    }
}
