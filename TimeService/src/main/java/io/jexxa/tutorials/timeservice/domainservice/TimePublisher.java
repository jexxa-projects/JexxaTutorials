package io.jexxa.tutorials.timeservice.domainservice;

import java.time.LocalTime;

public interface TimePublisher
{
    void publish(LocalTime localTime);
}
