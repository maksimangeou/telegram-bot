package pro.sky.telegrambot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDemo {
    // JDBC URL, username и password для подключения к БД
    static final String JDBC_URL = "jdbc:mysql://localhost:3306/telegram_bot_db";
    static final String USERNAME = "telegram_user";
    static final String PASSWORD = "secure_password_123"; // замените на свой пароль

    public static void main(String[] args) {
        System.out.println("Testing connection to MySQL...");

        // Используем try-with-resources для автоматического закрытия соединения
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            System.out.println("Connection successful!");
            // Здесь можно делать SQL-запросы...

        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }
}
