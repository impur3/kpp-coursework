package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Підключення до локальної бази MySQL. */
public final class DBUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/construction_defects";
    private static final String USER = "root";
    // УВАГА: впишіть сюди пароль користувача root, який ви задали під час встановлення MySQL.
    // Не завантажуйте справжній пароль на GitHub.
    private static final String PASSWORD = "";

    static {
        try {
            // Явна реєстрація драйвера потрібна для надійної роботи в Tomcat.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
