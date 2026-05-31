package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class FoodUtil {

    private static final String[] PREFERRED_ORDER = {
            "NEW",
            "SUPER MEAL",
            "HAPPY MEAL",
            "MEAL",
            "BURGER",
            "FRIES",
            "DESSERT",
            "DRINKS"
    };

    private FoodUtil() {
    }

    public static ArrayList<String> getCategories() {

        ArrayList<String> categories = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT DISTINCT category FROM foods " +
                    "WHERE category IS NOT NULL AND TRIM(category) <> ''";

            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String category = rs.getString("category");

                if (category != null && !category.trim().isEmpty()) {
                    categories.add(category.trim().toUpperCase());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        categories.sort(Comparator
                .comparingInt(FoodUtil::preferredIndex)
                .thenComparing(String::compareToIgnoreCase));

        return unique(categories);
    }

    public static ArrayList<FoodItem> getFoodItemsByCategory(String category) {

        ArrayList<FoodItem> foods = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT name, category, price, " +
                    "COALESCE(price_w_vat, ROUND(price * 1.12, 2)) AS price_with_vat, " +
                    "image_path FROM foods " +
                    "WHERE LOWER(category) LIKE ? " +
                    "ORDER BY name";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + normalizeCategory(category) + "%");

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                foods.add(new FoodItem(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("price"),
                        rs.getDouble("price_with_vat"),
                        rs.getString("image_path")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return foods;
    }

    public static ArrayList<String> getFoodsByCategory(String category) {

        ArrayList<String> names = new ArrayList<>();

        for (FoodItem item : getFoodItemsByCategory(category)) {
            names.add(item.getName());
        }

        return names;
    }

    public static HashMap<String, Integer> getPriceMap() {

        HashMap<String, Integer> prices = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT name, price FROM foods";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                prices.put(rs.getString("name"), rs.getInt("price"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return prices;
    }

    public static int getPrice(String foodName) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT price FROM foods WHERE name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, foodName);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt("price");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static double getPriceWithVat(String foodName) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT COALESCE(price_w_vat, ROUND(price * 1.12, 2)) AS price_with_vat " +
                    "FROM foods WHERE name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, foodName);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price_with_vat");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static double getFoodCost(String foodName) {

        double totalCost = 0;

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "SELECT fi.quantity, i.cost " +
                    "FROM food_ingredients fi " +
                    "JOIN ingredients i ON fi.ingredient_name = i.name " +
                    "WHERE fi.food_name = ?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, foodName);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                totalCost += rs.getDouble("quantity") * rs.getDouble("cost");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalCost;
    }

    public static double getProfit(String foodName) {
        return getPrice(foodName) - getFoodCost(foodName);
    }

    public static boolean addFood(String name, String category, int price) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "INSERT INTO foods (name, category, price, price_w_vat) " +
                    "VALUES (?, ?, ?, ROUND(? * 1.12, 2))";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setInt(3, price);
            pst.setInt(4, price);

            if (pst.executeUpdate() <= 0) {
                return false;
            }

            String imageSql =
                    "UPDATE foods SET image_path=CONCAT('src/images/', id, '.png') " +
                    "WHERE name=?";
            PreparedStatement imagePst = conn.prepareStatement(imageSql);
            imagePst.setString(1, name);

            return imagePst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateFood(
            String originalName,
            String name,
            String category,
            int price
    ) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "UPDATE foods SET name=?, category=?, price=?, " +
                    "price_w_vat=ROUND(? * 1.12, 2) " +
                    "WHERE name=?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setInt(3, price);
            pst.setInt(4, price);
            pst.setString(5, originalName);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFood(String name) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "DELETE FROM foods WHERE name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addIngredient(String name, double cost) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "INSERT INTO ingredients (name, cost) VALUES (?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setDouble(2, cost);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateIngredient(
            String originalName,
            String name,
            double cost
    ) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                    "UPDATE ingredients SET name=?, cost=? " +
                    "WHERE name=?";

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);
            pst.setDouble(2, cost);
            pst.setString(3, originalName);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteIngredient(String name) {

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "DELETE FROM ingredients WHERE name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, name);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String normalizeCategory(String category) {

        String fixedCategory = category == null ? "" : category.toLowerCase().trim();

        if (fixedCategory.endsWith("s")) {
            fixedCategory = fixedCategory.substring(0, fixedCategory.length() - 1);
        }

        return fixedCategory;
    }

    private static int preferredIndex(String category) {

        for (int i = 0; i < PREFERRED_ORDER.length; i++) {
            if (PREFERRED_ORDER[i].equalsIgnoreCase(category)) {
                return i;
            }
        }

        return PREFERRED_ORDER.length;
    }

    private static ArrayList<String> unique(ArrayList<String> values) {

        ArrayList<String> uniqueValues = new ArrayList<>();

        for (String value : values) {
            if (!uniqueValues.contains(value)) {
                uniqueValues.add(value);
            }
        }

        return uniqueValues;
    }
}
