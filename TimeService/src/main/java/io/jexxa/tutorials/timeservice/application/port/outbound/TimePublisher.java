package io.jexxa.tutorials.timeservice.application.port.outbound;

import java.time.LocalTime;

public interface TimePublisher
{
    void publish(LocalTime localTime);
}
