package com.linknest.backend.common.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {
    private DateTimeUtils() {}
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    public static String toIso(Instant instant) {
        return instant == null ? null : ISO.format(instant);
    }
}
