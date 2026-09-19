package com.nbsh.commerceapi.order;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd"
            );

    public String generate() {

        String date =
                LocalDate.now(
                        ZoneOffset.UTC
                ).format(DATE_FORMAT);

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        return "ORD-"
                + date
                + "-"
                + randomPart;
    }
}