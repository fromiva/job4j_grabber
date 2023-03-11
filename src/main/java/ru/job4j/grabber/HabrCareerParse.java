package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;
    private final int pageNumberLimit = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        for (int pageNumber = 1; pageNumber <= pageNumberLimit; pageNumber++) {
            parsePage(String.format("%s%d", link, pageNumber))
                    .stream()
                    .map(this::retrieveVacancy)
                    .forEach(result::add);
        }
        return result;
    }

    public static void main(String[] args) {
        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        parse.list(PAGE_LINK).forEach(System.out::println);
    }

    private static Elements parsePage(String link) {
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document == null) {
            throw new IllegalArgumentException();
        }
        return document.select(".vacancy-card__inner");
    }

    private Post retrieveVacancy(Element vacancy) {
        Post post = new Post();
        Element titleElement = vacancy.select(".vacancy-card__title").first();
        post.setTitle(titleElement.text());
        post.setLink(String.format("%s%s", SOURCE_LINK, titleElement.child(0).attr("href")));
        post.setCreated(dateTimeParser.parse(vacancy.select(".basic-date").first().attr("datetime")));
        post.setDescription(retrieveDescription(post.getLink()));
        return post;
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document != null ? document.select(".vacancy-description__text").first().text() : "";
    }
}
