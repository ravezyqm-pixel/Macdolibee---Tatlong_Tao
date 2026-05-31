package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardUtil {

    private DashboardUtil() {
    }

    public static int getTotalOrders() {
        return getCount("orders");
    }

    public static int getTotalFoods() {
        return getCount("foods");
    }

    public static int getTotalUnitsOrdered() {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT COALESCE(SUM(unit_count), 0) FROM orders";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getDailyDishesOrdered() {
        return getDishesOrdered(
                "DATE(order_time) = CURDATE()"
        );
    }

    public static int getWeeklyDishesOrdered() {
        return getDishesOrdered(
                "YEARWEEK(order_time, 1) = YEARWEEK(CURDATE(), 1)"
        );
    }

    public static int getMonthlyDishesOrdered() {
        return getDishesOrdered(
                "YEAR(order_time) = YEAR(CURDATE()) " +
                        "AND MONTH(order_time) = MONTH(CURDATE())"
        );
    }

    public static int getYearlyDishesOrdered() {
        return getDishesOrdered(
                "YEAR(order_time) = YEAR(CURDATE())"
        );
    }

    public static int getDailyOrderCount() {
        return getOrderCount(
                "DATE(order_time) = CURDATE()"
        );
    }

    public static int getWeeklyOrderCount() {
        return getOrderCount(
                "YEARWEEK(order_time, 1) = YEARWEEK(CURDATE(), 1)"
        );
    }

    public static int getMonthlyOrderCount() {
        return getOrderCount(
                "YEAR(order_time) = YEAR(CURDATE()) " +
                        "AND MONTH(order_time) = MONTH(CURDATE())"
        );
    }

    public static int getDailyCompletedOrders() {
        return getCompletedOrderCount(
                "DATE(completed_at) = CURDATE()"
        );
    }

    public static int getWeeklyCompletedOrders() {
        return getCompletedOrderCount(
                "YEARWEEK(completed_at, 1) = YEARWEEK(CURDATE(), 1)"
        );
    }

    public static int getMonthlyCompletedOrders() {
        return getCompletedOrderCount(
                "YEAR(completed_at) = YEAR(CURDATE()) " +
                        "AND MONTH(completed_at) = MONTH(CURDATE())"
        );
    }

    public static int getDailyCompletedSales() {
        return getCompletedSales(
                "DATE(completed_at) = CURDATE()"
        );
    }

    public static String getBestFoodsTodaySummary() {

        HashMap<String, Integer> foodCounts = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT product_list FROM orders " +
                    "WHERE status='COMPLETED' " +
                    "AND completed_at IS NOT NULL " +
                    "AND DATE(completed_at) = CURDATE()";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                addProductCounts(foodCounts, rs.getString("product_list"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (foodCounts.isEmpty()) {
            return "No completed orders today";
        }

        LinkedHashMap<String, Integer> sortedFoods = foodCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        StringBuilder summary = new StringBuilder();
        int rank = 1;

        for (Map.Entry<String, Integer> entry : sortedFoods.entrySet()) {
            summary.append(rank++)
                    .append(". ")
                    .append(entry.getKey())
                    .append(" x")
                    .append(entry.getValue())
                    .append("\n");
        }

        return summary.toString().trim();
    }

    public static String getBestSalesSummary() {

        ArrayList<Object[]> rows = getBestFoodRows();

        if (rows.isEmpty()) {
            return "No completed sales yet";
        }

        StringBuilder summary = new StringBuilder();

        for (Object[] row : rows) {
            summary.append(row[0])
                    .append(". ")
                    .append(row[1])
                    .append(" - ")
                    .append(row[2])
                    .append(" sold")
                    .append("\n");
        }

        return summary.toString().trim();
    }

    public static ArrayList<Object[]> getBestFoodRows() {

        HashMap<String, Integer> foodCounts = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT product_list FROM orders " +
                    "WHERE status='COMPLETED'";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                addProductCounts(foodCounts, rs.getString("product_list"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Object[]> rows = new ArrayList<>();
        int rank = 1;

        for (Map.Entry<String, Integer> entry : foodCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toList())) {

            rows.add(new Object[]{
                    rank++,
                    entry.getKey(),
                    entry.getValue()
            });
        }

        return rows;
    }

    public static String getBestDailyOrderSummary() {

        HashMap<String, Integer> prices = FoodUtil.getPriceMap();
        int bestOrderId = 0;
        int bestValue = -1;
        int bestUnits = 0;
        String bestProducts = "";

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT id, unit_count, product_list, sub_total " +
                    "FROM orders WHERE DATE(order_time) = CURDATE()";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String products = rs.getString("product_list");
                int value = rs.getInt("sub_total");

                if (value <= 0) {
                    value = computeOrderValue(products, prices);
                }

                if (value > bestValue) {
                    bestValue = value;
                    bestOrderId = rs.getInt("id");
                    bestUnits = rs.getInt("unit_count");
                    bestProducts = products;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bestOrderId == 0) {
            return "No orders today";
        }

        return "ORDER #" + bestOrderId
                + "\nSPENT: " + UIUtil.money(bestValue)
                + "\nDISHES: " + bestUnits
                + "\n" + bestProducts;
    }

    public static String getBestEmployeeOfYear() {
        return "QUIAMCO";
    }

    public static String getUsersAndAdminsSummary() {

        StringBuilder summary = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT username, role FROM users " +
                    "ORDER BY FIELD(role, 'admin', 'employee'), username";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String role = rs.getString("role");
                String username = rs.getString("username");

                summary.append(username)
                        .append(" (")
                        .append(role == null ? "employee" : role.toUpperCase())
                        .append(")")
                        .append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (summary.length() == 0) {
            return "No users or admins found";
        }

        return summary.toString().trim();
    }

    public static ArrayList<Object[]> getUsersAndAdminsRows() {

        ArrayList<Object[]> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT id, username, role FROM users " +
                    "ORDER BY FIELD(role, 'admin', 'employee'), username";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String role = rs.getString("role");

                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        formatRole(role)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    public static boolean addUser(String username, String password, String role) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, normalizeStoredRole(role));

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateUser(
            int id,
            String username,
            String password,
            String role
    ) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql;
            PreparedStatement pst;

            if (password == null || password.trim().isEmpty()) {
                sql = "UPDATE users SET username=?, role=? WHERE id=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, normalizeStoredRole(role));
                pst.setInt(3, id);
            } else {
                sql = "UPDATE users SET username=?, password=?, role=? WHERE id=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setString(3, normalizeStoredRole(role));
                pst.setInt(4, id);
            }

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean deleteUser(int id) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static OrderRecord getLatestOrder() {

        ArrayList<OrderRecord> records = getOrderRecords();

        if (records.isEmpty()) {
            return null;
        }

        return records.get(0);
    }

    public static ArrayList<OrderRecord> getOrderRecords() {

        ArrayList<OrderRecord> records = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT id, unit_count, sub_total, product_list, barcode, order_time, status " +
                    "FROM orders ORDER BY id DESC";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                records.add(new OrderRecord(
                        rs.getInt("id"),
                        rs.getInt("unit_count"),
                        rs.getInt("sub_total"),
                        rs.getTimestamp("order_time"),
                        rs.getString("product_list"),
                        rs.getString("barcode"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    private static int getDishesOrdered(String whereClause) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT COALESCE(SUM(unit_count), 0) " +
                    "FROM orders WHERE " + whereClause;

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int getOrderCount(String whereClause) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT COUNT(*) FROM orders WHERE " + whereClause;

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int getCompletedOrderCount(String dateClause) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT COUNT(*) FROM orders " +
                    "WHERE status='COMPLETED' " +
                    "AND completed_at IS NOT NULL " +
                    "AND " + dateClause;

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static int getCompletedSales(String dateClause) {

        HashMap<String, Integer> prices = FoodUtil.getPriceMap();
        int total = 0;

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT sub_total, product_list FROM orders " +
                    "WHERE status='COMPLETED' " +
                    "AND completed_at IS NOT NULL " +
                    "AND " + dateClause;

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int subTotal = rs.getInt("sub_total");

                if (subTotal <= 0) {
                    subTotal = computeOrderValue(
                            rs.getString("product_list"),
                            prices
                    );
                }

                total += subTotal;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    private static int computeOrderValue(
            String productList,
            HashMap<String, Integer> prices
    ) {

        if (productList == null || productList.trim().isEmpty()) {
            return 0;
        }

        int total = 0;
        String[] entries = productList.split(",");

        for (String rawEntry : entries) {
            String entry = rawEntry.trim();

            if (entry.isEmpty()) {
                continue;
            }

            String foodName = entry;
            int qty = 1;
            int qtyIndex = entry.lastIndexOf(" x");

            if (qtyIndex >= 0) {
                foodName = entry.substring(0, qtyIndex).trim();
                String qtyText = entry.substring(qtyIndex + 2).trim();

                try {
                    qty = Integer.parseInt(qtyText);
                } catch (NumberFormatException e) {
                    qty = 1;
                }
            }

            total += findPrice(foodName, prices) * qty;
        }

        return total;
    }

    private static void addProductCounts(
            HashMap<String, Integer> foodCounts,
            String productList
    ) {

        if (productList == null || productList.trim().isEmpty()) {
            return;
        }

        String[] entries = productList.split(",");

        for (String rawEntry : entries) {
            String entry = rawEntry.trim();

            if (entry.isEmpty()) {
                continue;
            }

            String foodName = entry;
            int qty = 1;
            int qtyIndex = entry.lastIndexOf(" x");

            if (qtyIndex >= 0) {
                foodName = entry.substring(0, qtyIndex).trim();
                String qtyText = entry.substring(qtyIndex + 2).trim();

                try {
                    qty = Integer.parseInt(qtyText);
                } catch (NumberFormatException e) {
                    qty = 1;
                }
            }

            foodCounts.put(
                    foodName,
                    foodCounts.getOrDefault(foodName, 0) + qty
            );
        }
    }

    private static int findPrice(String foodName, HashMap<String, Integer> prices) {

        if (prices.containsKey(foodName)) {
            return prices.get(foodName);
        }

        for (Map.Entry<String, Integer> entry : prices.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(foodName)) {
                return entry.getValue();
            }
        }

        return 0;
    }

    private static String formatRole(String role) {

        if (role == null || role.trim().isEmpty()) {
            return "Employee";
        }

        if ("admin".equalsIgnoreCase(role)) {
            return "Admin";
        }

        return "Employee";
    }

    private static String normalizeStoredRole(String role) {

        if (role != null && role.equalsIgnoreCase("admin")) {
            return "admin";
        }

        return "employee";
    }

    private static int getCount(String tableName) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT COUNT(*) FROM " + tableName;
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
