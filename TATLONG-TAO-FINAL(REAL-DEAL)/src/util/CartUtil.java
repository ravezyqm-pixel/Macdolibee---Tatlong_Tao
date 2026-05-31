package util;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartUtil {

    private static final LinkedHashMap<String, Integer> cart =
            new LinkedHashMap<>();

    private static String orderType = "DINE IN";

    private CartUtil() {
    }

    public static void setOrderType(String type) {
        orderType = type;
    }

    public static String getOrderType() {
        return orderType;
    }

    public static void addItem(String food) {
        cart.put(food, getQuantity(food) + 1);
    }

    public static void removeItem(String food) {
        int qty = getQuantity(food);

        if (qty <= 1) {
            cart.remove(food);
        } else {
            cart.put(food, qty - 1);
        }
    }

    public static int getQuantity(String food) {
        return cart.getOrDefault(food, 0);
    }

    public static LinkedHashMap<String, Integer> getCart() {
        return cart;
    }

    public static int getTotalUnits() {
        int total = 0;

        for (int qty : cart.values()) {
            total += qty;
        }

        return total;
    }

    public static int getSubtotal() {
        return (int) Math.round(getSubtotalWithVat());
    }

    public static double getSubtotalWithVat() {
        double total = 0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            total += FoodUtil.getPriceWithVat(entry.getKey()) * entry.getValue();
        }

        return total;
    }

    public static void clearCart() {
        cart.clear();
    }
}
