package io.jexxa.tutorials.timeservice.infrastructure.drivenadapter.display;

import io.jexxa.common.wrapper.logger.SLF4jLogger;
import io.jexxa.tutorials.timeservice.domainservice.TimeDisplay;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class TimeDisplayImpl implements TimeDisplay
{
    @Override
    public void show(LocalTime localTime)
    {
        var messageWithPublishedTime = "New Time was published, time: " + localTime.format(DateTimeFormatter.ISO_TIME);
        SLF4jLogger.getLogger(TimeDisplayImpl.class).info(messageWithPublishedTime);
    }
}
