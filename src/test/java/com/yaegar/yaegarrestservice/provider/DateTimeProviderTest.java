package com.yaegar.yaegarrestservice.provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.is;

public class DateTimeProviderTest {
    private LocalDateTime localDateTime = LocalDateTime.of(2019, 3, 14, 12, 28, 5);
    private DateTimeProvider sut;

    @Before
    public void setup() {
        sut = new DateTimeProvider(Clock.fixed(localDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC")));
    }

    @Test
    public void shouldReturnNow() {
        //given
        LocalDateTime expectedDateTime = localDateTime;

        //when
        LocalDateTime actualDateTime = sut.now();

        //then
        Assert.assertThat(actualDateTime, is(expectedDateTime));
    }
}
