package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthUtil {

    public static String checkLogin(
            String username,
            String password
    ) {

        try (
                Connection conn =
                        DBConnection.getConnection()
        ) {

            String sql =
                    "SELECT role FROM users " +
                    "WHERE username=? AND password=?";

            PreparedStatement pst =
                    conn.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}