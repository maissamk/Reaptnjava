package models;

import java.util.Date;

public class ProductType {
    private int id;
    private String season;
    private String productionMethod;
    private Date harvestDate;
    private String preservationDuration;

    public ProductType() {
    }

    public ProductType(String season, String productionMethod, Date harvestDate, String preservationDuration) {
        this.season = season;
        this.productionMethod = productionMethod;
        this.harvestDate = harvestDate;
        this.preservationDuration = preservationDuration;
    }

    public ProductType(int id, String season, String productionMethod, Date harvestDate, String preservationDuration) {
        this.id = id;
        this.season = season;
        this.productionMethod = productionMethod;
        this.harvestDate = harvestDate;
        this.preservationDuration = preservationDuration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getProductionMethod() {
        return productionMethod;
    }

    public void setProductionMethod(String productionMethod) {
        this.productionMethod = productionMethod;
    }

    public Date getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(Date harvestDate) {
        this.harvestDate = harvestDate;
    }

    public String getPreservationDuration() {
        return preservationDuration;
    }

    public void setPreservationDuration(String preservationDuration) {
        this.preservationDuration = preservationDuration;
    }

    @Override
    public String toString() {
        return "ProductType{" +
                "id=" + id +
                ", season='" + season + '\'' +
                ", productionMethod='" + productionMethod + '\'' +
                ", harvestDate=" + harvestDate +
                ", preservationDuration='" + preservationDuration + '\'' +
                '}';
    }
} 