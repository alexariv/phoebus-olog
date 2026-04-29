package org.phoebus.olog.ai;

import org.phoebus.olog.entity.Log;
import org.springframework.context.ApplicationEvent;

public class LogEntryCreatedEvent extends ApplicationEvent {

    private final Log log;

    public LogEntryCreatedEvent(Object source, Log log) {
        super(source);
        this.log = log;
    }

    public Log getLog() {
        return log;
    }
}