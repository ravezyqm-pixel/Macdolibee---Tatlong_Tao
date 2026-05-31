package util;

public class VATUtil {
  public static double computeVAT(double amount) {
    return amount * 0.12;
}

public static double costWithVAT(double cost) {
    return cost + computeVAT(cost);
}  
}
