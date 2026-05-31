package util;

import java.sql.*;

public class DBConnection {

    private static final String URL =
        "jdbc:mysql://127.0.0.1:3306/macdolibee2"
        + "?useSSL=false"
        + "&allowPublicKeyRetrieval=true"
        + "&serverTimezone=Asia%2FManila";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    public static Connection getConnection()
            throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL Connector/J is missing from the project classpath. "
                    + "Make sure lib/mysql-connector-j-9.6.0.jar exists.",
                    e
            );
        }

        return DriverManager.getConnection(
            URL,
            USER,
            PASSWORD
        );
    }
}
