package com.flowiq.utils;

import net.datafaker.Faker;

import java.util.Locale;
import java.util.UUID;

public final class RandomDataGenerator {

    private static final Faker FAKER = new Faker(Locale.forLanguageTag("en-US"));

    private RandomDataGenerator() {
    }

    public static Faker faker() {
        return FAKER;
    }

    public static String email() {
        return FAKER.internet().emailAddress();
    }

    public static String password(int length) {
        return FAKER.internet().password(length, length, true, true, true);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String companyName() {
        return FAKER.company().name();
    }

    public static String firstName() {
        return FAKER.name().firstName();
    }

    public static String lastName() {
        return FAKER.name().lastName();
    }

    public static String alphanumeric(int length) {
        return FAKER.regexify("[A-Za-z0-9]{" + length + "}");
    }

    public static int randomInt(int min, int max) {
        return FAKER.number().numberBetween(min, max);
    }
}
