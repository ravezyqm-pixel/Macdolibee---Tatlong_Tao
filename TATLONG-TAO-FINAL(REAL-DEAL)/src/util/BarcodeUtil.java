package util;

public class BarcodeUtil {

    public static String generateBarcodeID() {

        int number =
                10000 +
                (int)(Math.random() * 90000);

        return "MACDOLIBEE_" + number;
    }
}