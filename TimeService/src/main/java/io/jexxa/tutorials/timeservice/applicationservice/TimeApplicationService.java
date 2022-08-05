package io.jexxa.tutorials.timeservice.applicationservice;

import io.jexxa.tutorials.timeservice.domainservice.MessageDisplay;
import io.jexxa.tutorials.timeservice.domainservice.TimePublisher;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@SuppressWarnings("unused")
public class TimeApplicationService
{
    private final TimePublisher timePublisher;
    private final MessageDisplay messageDisplay;

    /**
     * Note: Jexxa supports only implicit constructor injection. Therefore, we must
     * declare all required interfaces in the constructor.
     *
     * @param timePublisher required outbound port for this application service
     * @param messageDisplay required outbound port for this application service
     */
    public TimeApplicationService(TimePublisher timePublisher, MessageDisplay messageDisplay)
    {
        this.timePublisher = Objects.requireNonNull(timePublisher);
        this.messageDisplay = Objects.requireNonNull(messageDisplay);
    }

    public LocalTime getTime()
    {
        return LocalTime.now();
    }

    public void publishTime()
    {
        timePublisher.publish(getTime());
    }


    /**
     * This method shows the previously published time.
     * @param localTime the previously published time
     */
    public void displayPublishedTime(LocalTime localTime)
    {
        var messageWithPublishedTime = "New Time was published, time: " + localTime.format(DateTimeFormatter.ISO_TIME);
        messageDisplay.show(messageWithPublishedTime);
    }
}
