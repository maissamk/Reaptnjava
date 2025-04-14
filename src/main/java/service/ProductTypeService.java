package service;

import models.ProductType;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductTypeService {
    private Connection connection;

    public ProductTypeService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    public void add(ProductType productType) throws SQLException {
        String query = "INSERT INTO product_type (season, production_method, harvest_date, preservation_duration) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, productType.getSeason());
        ps.setString(2, productType.getProductionMethod());
        ps.setDate(3, new java.sql.Date(productType.getHarvestDate().getTime()));
        ps.setString(4, productType.getPreservationDuration());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            productType.setId(rs.getInt(1));
        }
        ps.close();
    }

    public void update(ProductType productType) throws SQLException {
        String query = "UPDATE product_type SET season = ?, production_method = ?, harvest_date = ?, preservation_duration = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, productType.getSeason());
        ps.setString(2, productType.getProductionMethod());
        ps.setDate(3, new java.sql.Date(productType.getHarvestDate().getTime()));
        ps.setString(4, productType.getPreservationDuration());
        ps.setInt(5, productType.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM product_type WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public ProductType getById(int id) throws SQLException {
        String query = "SELECT * FROM product_type WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        
        ProductType productType = null;
        if (rs.next()) {
            productType = new ProductType(
                rs.getInt("id"),
                rs.getString("season"),
                rs.getString("production_method"),
                rs.getDate("harvest_date"),
                rs.getString("preservation_duration")
            );
        }
        
        rs.close();
        ps.close();
        return productType;
    }

    public List<ProductType> getAll() throws SQLException {
        List<ProductType> productTypes = new ArrayList<>();
        String query = "SELECT * FROM product_type";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        
        while (rs.next()) {
            ProductType productType = new ProductType(
                rs.getInt("id"),
                rs.getString("season"),
                rs.getString("production_method"),
                rs.getDate("harvest_date"),
                rs.getString("preservation_duration")
            );
            productTypes.add(productType);
        }
        
        rs.close();
        st.close();
        return productTypes;
    }
} 