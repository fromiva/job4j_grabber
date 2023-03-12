package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private Connection connection;

    public PsqlStore(Properties configuration) {
        try {
            Class.forName(configuration.getProperty("jdbc.driver"));
            connection = DriverManager.getConnection(
                    configuration.getProperty("jdbc.url"),
                    configuration.getProperty("jdbc.username"),
                    configuration.getProperty("jdbc.password"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection
                .prepareStatement("insert into posts (title, link, description, created)"
                        + "values (?, ? ,? ,?) on conflict (link) do nothing")) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from posts")) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    posts.add(createPost(result));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from posts where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    post = createPost(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private Post createPost(ResultSet set) throws SQLException {
        return new Post(
                set.getInt("id"),
                set.getString("title"),
                set.getString("link"),
                set.getString("description"),
                set.getTimestamp("created").toLocalDateTime());
    }

    public static void main(String[] args) {
        Properties configuration = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            configuration.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        try (Store store = new PsqlStore(configuration)) {
            System.out.println("Retrieving vacancies from web...");
            parse.list().forEach(store::save);

            System.out.println("Get vacancies from DB...");
            store.getAll().forEach(System.out::println);

            System.out.println("Get vacancy with id=29 from DB...");
            System.out.println(store.findById(29));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
