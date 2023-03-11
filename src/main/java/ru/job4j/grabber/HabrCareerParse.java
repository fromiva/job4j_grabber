package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Elements rows = new Elements();
        int numberOfPages = 5;
        for (int pageNuber = 1; pageNuber <= numberOfPages; pageNuber++) {
            String link = String.format("%s?page=%d", PAGE_LINK, pageNuber);
            rows.addAll(parsePage(link));
        }
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String vacancyDate = row.select(".basic-date").first().attr("datetime");
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String vacancyDescription = retrieveDescription(link);
            System.out.printf("%s %s %s%n", vacancyName, vacancyDate, link);
            System.out.println(vacancyDescription);
            System.out.println();
        });
    }

    private static Elements parsePage(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        return document.select(".vacancy-card__inner");
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
