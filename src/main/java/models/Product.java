package models;

public class Product {
    private int id;
    private String category;
    private ProductType type;
    private int quantity;
    private float weight;
    private double price;
    private String image;

    public Product() {
    }

    public Product(String category, ProductType type, int quantity, float weight, double price, String image) {
        this.category = category;
        this.type = type;
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
        this.image = image;
    }

    public Product(int id, String category, ProductType type, int quantity, float weight, double price, String image) {
        this.id = id;
        this.category = category;
        this.type = type;
        this.quantity = quantity;
        this.weight = weight;
        this.price = price;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", type=" + type +
                ", quantity=" + quantity +
                ", weight=" + weight +
                ", price=" + price +
                ", image='" + image + '\'' +
                '}';
    }
} 