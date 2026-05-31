package util;

import java.sql.Timestamp;

public class OrderRecord {

    private final int id;
    private final int unitCount;
    private final int subTotal;
    private final Timestamp orderTime;
    private final String productList;
    private final String barcode;
    private final String status;

    public OrderRecord(
            int id,
            int unitCount,
            int subTotal,
            Timestamp orderTime,
            String productList,
            String barcode,
            String status
    ) {
        this.id = id;
        this.unitCount = unitCount;
        this.subTotal = subTotal;
        this.orderTime = orderTime;
        this.productList = productList;
        this.barcode = barcode;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getUnitCount() {
        return unitCount;
    }

    public int getSubTotal() {
        return subTotal;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public String getProductList() {
        return productList;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getStatus() {
        return status;
    }
}
