package service;

import models.Product;
import models.ProductType;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private Connection connection;
    private ProductTypeService productTypeService;
    
    public ProductService() {
        connection = MyDataBase.getInstance().getConnection();
        productTypeService = new ProductTypeService();
    }
    
    public void add(Product product) throws SQLException {
        String query = "INSERT INTO product (category, type_id, quantity, weight, price, image) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, product.getCategory());
            ps.setInt(2, product.getType().getId());
            ps.setInt(3, product.getQuantity());
            ps.setFloat(4, product.getWeight());
        ps.setDouble(5, product.getPrice());
            ps.setString(6, product.getImage());
        ps.executeUpdate();
            
        ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        product.setId(rs.getInt(1));
                    }
        ps.close();
        }

    public void update(Product product) throws SQLException {
        String query = "UPDATE product SET category = ?, type_id = ?, quantity = ?, weight = ?, price = ?, image = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, product.getCategory());
            ps.setInt(2, product.getType().getId());
            ps.setInt(3, product.getQuantity());
            ps.setFloat(4, product.getWeight());
        ps.setDouble(5, product.getPrice());
            ps.setString(6, product.getImage());
            ps.setInt(7, product.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM product WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
        }

    public Product getById(int id) throws SQLException {
        String query = "SELECT * FROM product WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        
        Product product = null;
        if (rs.next()) {
            ProductType productType = productTypeService.getById(rs.getInt("type_id"));
            
            product = new Product(
                rs.getInt("id"),
                rs.getString("category"),
                productType,
                rs.getInt("quantity"),
                rs.getFloat("weight"),
                rs.getDouble("price"),
                rs.getString("image")
            );
        }
        
        rs.close();
        ps.close();
        return product;
    }
    
    public List<Product> getAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM product";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        
        while (rs.next()) {
            ProductType productType = productTypeService.getById(rs.getInt("type_id"));
            
            Product product = new Product(
                rs.getInt("id"),
                rs.getString("category"),
                productType,
                rs.getInt("quantity"),
                rs.getFloat("weight"),
                rs.getDouble("price"),
                rs.getString("image")
            );
            products.add(product);
        }
        
        rs.close();
        st.close();
        return products;
    }
    
    public List<Product> getByCategoryOrType(String category, int typeId) throws SQLException {
        List<Product> products = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM product WHERE 1=1");
        
        if (category != null && !category.isEmpty()) {
            queryBuilder.append(" AND category = ?");
        }
        
        if (typeId > 0) {
            queryBuilder.append(" AND type_id = ?");
        }
        
        PreparedStatement ps = connection.prepareStatement(queryBuilder.toString());
        
        int paramIndex = 1;
        if (category != null && !category.isEmpty()) {
            ps.setString(paramIndex++, category);
        }
        
        if (typeId > 0) {
            ps.setInt(paramIndex, typeId);
        }
        
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            ProductType productType = productTypeService.getById(rs.getInt("type_id"));
            
            Product product = new Product(
                rs.getInt("id"),
                rs.getString("category"),
                productType,
                rs.getInt("quantity"),
                rs.getFloat("weight"),
                rs.getDouble("price"),
                rs.getString("image")
            );
            products.add(product);
        }
        
        rs.close();
        ps.close();
        return products;
    }
} 