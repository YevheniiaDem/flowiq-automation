package com.flowiq.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateUtils() {
    }

    public static String today() {
        return LocalDate.now().format(ISO_DATE);
    }

    public static String tomorrow() {
        return LocalDate.now().plusDays(1).format(ISO_DATE);
    }

    public static String daysFromNow(int days) {
        return LocalDate.now().plusDays(days).format(ISO_DATE);
    }

    public static String daysAgo(int days) {
        return LocalDate.now().minusDays(days).format(ISO_DATE);
    }

    public static String nowIso() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(ISO_DATE_TIME);
    }

    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, ISO_DATE);
    }
}
