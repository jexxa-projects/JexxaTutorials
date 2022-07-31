package io.jexxa.tutorials.timeservice.infrastructure.drivenadapter.messaging;

import io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSender;
import io.jexxa.tutorials.timeservice.domainservice.TimePublisher;

import java.time.LocalTime;
import java.util.Properties;

import static io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSenderManager.getMessageSender;

@SuppressWarnings("unused")
public class TimePublisherImpl implements TimePublisher
{
    public static final String TIME_TOPIC = "TimeService";

    private final MessageSender messageSender;

    // For all driven adapter we have to provide either a static factory or a public constructor to
    // enable implicit constructor injection
    public TimePublisherImpl(Properties properties)
    {
        //Request a default message Sender from corresponding strategy manager
        this.messageSender = getMessageSender(TimePublisher.class, properties);
    }

    @Override
    public void publish(LocalTime localTime)
    {
        // Send the message to the topic.
        messageSender.send(localTime)
                .toTopic(TIME_TOPIC)
                .asJson();
    }
}
