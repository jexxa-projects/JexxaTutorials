package io.jexxa.tutorials.timeservice.infrastructure.drivenadapter.display;

import io.jexxa.common.wrapper.logger.SLF4jLogger;
import io.jexxa.tutorials.timeservice.domainservice.MessageDisplay;

@SuppressWarnings("unused")
public class MessageDisplayImpl implements MessageDisplay
{
    @Override
    public void show(String message)
    {
        SLF4jLogger.getLogger(MessageDisplayImpl.class).info(message);
    }
}
