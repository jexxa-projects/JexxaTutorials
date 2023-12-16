package io.jexxa.tutorials.timeservice.applicationservice;

import io.jexxa.tutorials.timeservice.domainservice.TimeDisplay;
import io.jexxa.tutorials.timeservice.domainservice.TimePublisher;

import java.time.LocalTime;
import java.util.Objects;

@SuppressWarnings("unused")
public class TimeApplicationService
{
    private final TimePublisher timePublisher;
    private final TimeDisplay timeDisplay;

    /**
     * This class need a {@link TimePublisher} and {@link TimeDisplay} for proper working. Therefore, we must
     * declare all required interfaces in the constructor.
     *
     * @param timePublisher used to publish time.
     * @param timeDisplay used to show a received time
     */
    public TimeApplicationService(TimePublisher timePublisher, TimeDisplay timeDisplay)
    {
        this.timePublisher = Objects.requireNonNull(timePublisher);
        this.timeDisplay = Objects.requireNonNull(timeDisplay);
    }

    /**
     * Implements use case 1: publish current time
     */
    public void publishTime()
    {
        timePublisher.publish(LocalTime.now());
    }

    /**
     * Implements use case 2: Shows the previously published time.
     * @param localTime the previously published time
     */
    public void showReceivedTime(LocalTime localTime)
    {
        timeDisplay.show(localTime);
    }
}
