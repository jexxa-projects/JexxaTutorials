package io.jexxa.tutorials.timeservice.infrastructure.drivenadapter.display;

import io.jexxa.tutorials.timeservice.domainservice.MessageDisplay;
import io.jexxa.utils.JexxaLogger;

@SuppressWarnings("unused")
public class MessageDisplayImpl implements MessageDisplay
{
    @Override
    public void show(String message)
    {
        JexxaLogger.getLogger(MessageDisplayImpl.class).info(message);
    }
}
