package src.com.smartphonestore.model.product;

public abstract class Phone extends Product {
    private String operatingSystem;

    public Phone(String id, String name, String brand, double price, int stockQuantity, int minStockThreshold, String operatingSystem) {
        super(id, name, brand, price, stockQuantity, minStockThreshold);
        this.operatingSystem = operatingSystem;
    }

    // Hàm tính thời gian bảo hành
    @Override
    public int getWarrantyMonths() {
        if ("IOS".equalsIgnoreCase(operatingSystem) || "Apple".equalsIgnoreCase(getBrand())) {
            return 12;
        } else if ("Android".equalsIgnoreCase(operatingSystem)) {
            return 18;
        }
        return 6;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
}
