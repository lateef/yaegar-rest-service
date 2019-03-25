package com.yaegar.yaegarrestservice.provider;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class DateTimeProvider {
    private final Clock clock;

    public DateTimeProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
