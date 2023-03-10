package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HabrCareerDateTimeParserTest {

    @Test
    void whenParseWithTimeZoneThenCorrect() {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        String source = "2023-03-10T16:27:44+03:00";
        LocalDateTime expected = LocalDateTime.of(2023, 3, 10, 16, 27, 44);
        LocalDateTime actual = parser.parse(source);
        assertEquals(expected, actual);

    }

    @Test
    void whenParseWithoutTimeZoneThenCorrect() {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        String source = "2023-03-10T16:27:44";
        LocalDateTime expected = LocalDateTime.of(2023, 3, 10, 16, 27, 44);
        LocalDateTime actual = parser.parse(source);
        assertEquals(expected, actual);

    }
}