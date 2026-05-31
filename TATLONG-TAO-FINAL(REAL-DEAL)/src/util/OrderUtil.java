package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class OrderUtil {

    private OrderUtil() {
    }

    public static boolean saveOrder(String barcode) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "INSERT INTO orders " +
                    "(unit_count, sub_total, product_list, barcode, status) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, CartUtil.getTotalUnits());
            pst.setInt(2, CartUtil.getSubtotal());
            pst.setString(3, buildProductList());
            pst.setString(4, barcode);
            pst.setString(5, "ON-GOING");

            pst.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean completeOrder(int orderId) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "UPDATE orders SET status=?, completed_at=CURRENT_TIMESTAMP " +
                    "WHERE id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "COMPLETED");
            pst.setInt(2, orderId);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void backfillMissingSubTotals() {

        HashMap<String, Integer> prices = FoodUtil.getPriceMap();

        try (Connection conn = DBConnection.getConnection()) {

            String selectSql =
                    "SELECT id, product_list FROM orders " +
                    "WHERE sub_total IS NULL OR sub_total <= 0";

            PreparedStatement select = conn.prepareStatement(selectSql);
            ResultSet rs = select.executeQuery();

            String updateSql = "UPDATE orders SET sub_total=? WHERE id=?";
            PreparedStatement update = conn.prepareStatement(updateSql);

            while (rs.next()) {
                int subTotal = computeOrderValue(
                        rs.getString("product_list"),
                        prices
                );

                if (subTotal > 0) {
                    update.setInt(1, subTotal);
                    update.setInt(2, rs.getInt("id"));
                    update.addBatch();
                }
            }

            update.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String buildProductList() {

        StringJoiner products = new StringJoiner(", ");

        for (Map.Entry<String, Integer> entry : CartUtil.getCart().entrySet()) {
            products.add(entry.getKey() + " x" + entry.getValue());
        }

        return products.toString();
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
}
