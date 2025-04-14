package models;

import java.time.LocalDateTime;

public class Stock {
    private int id;
    private Product product;
    private int availableQuantity;
    private float stockMinimum;
    private float stockMaximum;
    private LocalDateTime entryDate;
    private LocalDateTime exitDate;

    public Stock() {
    }

    public Stock(Product product, int availableQuantity, float stockMinimum, float stockMaximum, LocalDateTime entryDate, LocalDateTime exitDate) {
        this.product = product;
        this.availableQuantity = availableQuantity;
        this.stockMinimum = stockMinimum;
        this.stockMaximum = stockMaximum;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
    }

    public Stock(int id, Product product, int availableQuantity, float stockMinimum, float stockMaximum, LocalDateTime entryDate, LocalDateTime exitDate) {
        this.id = id;
        this.product = product;
        this.availableQuantity = availableQuantity;
        this.stockMinimum = stockMinimum;
        this.stockMaximum = stockMaximum;
        this.entryDate = entryDate;
        this.exitDate = exitDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public float getStockMinimum() {
        return stockMinimum;
    }

    public void setStockMinimum(float stockMinimum) {
        this.stockMinimum = stockMinimum;
    }

    public float getStockMaximum() {
        return stockMaximum;
    }

    public void setStockMaximum(float stockMaximum) {
        this.stockMaximum = stockMaximum;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDateTime entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDateTime getExitDate() {
        return exitDate;
    }

    public void setExitDate(LocalDateTime exitDate) {
        this.exitDate = exitDate;
    }

    public boolean isLowStock() {
        return availableQuantity < stockMinimum;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", product=" + product +
                ", availableQuantity=" + availableQuantity +
                ", stockMinimum=" + stockMinimum +
                ", stockMaximum=" + stockMaximum +
                ", entryDate=" + entryDate +
                ", exitDate=" + exitDate +
                '}';
    }
} 