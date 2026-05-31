package util;

public class FoodItem {

    private final String name;
    private final String category;
    private final int price;
    private final double priceWithVat;
    private final String imagePath;

    public FoodItem(String name, String category, int price) {
        this(name, category, price, price * 1.12, "");
    }

    public FoodItem(String name, String category, int price, String imagePath) {
        this(name, category, price, price * 1.12, imagePath);
    }

    public FoodItem(
            String name,
            String category,
            int price,
            double priceWithVat,
            String imagePath
    ) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.priceWithVat = priceWithVat;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public double getPriceWithVat() {
        return priceWithVat;
    }

    public String getImagePath() {
        return imagePath;
    }
}
