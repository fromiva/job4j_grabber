package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        Properties configuration = getConfiguration();
        try (Connection connection = getConnection(configuration)) {
            Scheduler scheduller = StdSchedulerFactory.getDefaultScheduler();
            scheduller.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(
                            configuration.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduller.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduller.shutdown();
        } catch (SchedulerException | InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement = connection.prepareStatement("insert into rabbit (created_date) values (?)")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Properties getConfiguration() {
        Properties result = new Properties();
        try (InputStream in = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            result.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static Connection getConnection(Properties configuration) {
        Connection connection = null;
        try {
            Class.forName(configuration.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    configuration.getProperty("url"),
                    configuration.getProperty("username"),
                    configuration.getProperty("password"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
