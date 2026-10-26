package io.jexxa.tutorials.timeservice.application.port.outbound;

import java.time.LocalTime;

public interface TimeDisplay
{
    void show(LocalTime localTime);
}
