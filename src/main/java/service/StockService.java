package service;

import models.Product;
import models.Stock;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockService implements CrudService<Stock, Integer> {
    
    private Connection connection;
    private ProductService productService;
    
    public StockService() {
        this.connection = MyDataBase.getInstance().getConnection();
        this.productService = new ProductService();
    }
    
    @Override
    public Stock add(Stock stock) {
        String query = "INSERT INTO stock (product_id, available_quantity, stock_minimum, stock_maximum, " +
                       "entry_date, exit_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, stock.getProduct().getId());
            ps.setInt(2, stock.getAvailableQuantity());
            ps.setFloat(3, stock.getStockMinimum());
            ps.setFloat(4, stock.getStockMaximum());
            ps.setTimestamp(5, Timestamp.valueOf(stock.getEntryDate()));
            
            if (stock.getExitDate() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(stock.getExitDate()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        stock.setId(rs.getInt(1));
                    }
                }
            }
            
            return stock;
        } catch (SQLException e) {
            System.err.println("Error adding stock: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Stock update(Stock stock) {
        String query = "UPDATE stock SET product_id = ?, available_quantity = ?, stock_minimum = ?, " +
                       "stock_maximum = ?, entry_date = ?, exit_date = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, stock.getProduct().getId());
            ps.setInt(2, stock.getAvailableQuantity());
            ps.setFloat(3, stock.getStockMinimum());
            ps.setFloat(4, stock.getStockMaximum());
            ps.setTimestamp(5, Timestamp.valueOf(stock.getEntryDate()));
            
            if (stock.getExitDate() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(stock.getExitDate()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            
            ps.setInt(7, stock.getId());
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                return stock;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean delete(Integer id) {
        String query = "DELETE FROM stock WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            int affectedRows = ps.executeUpdate();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting stock: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public Stock findById(Integer id) {
        String query = "SELECT * FROM stock WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStock(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding stock by ID: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public List<Stock> findAll() {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT * FROM stock";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all stocks: " + e.getMessage());
        }
        
        return stocks;
    }
    
    public List<Stock> findLowStocks() {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT * FROM stock WHERE available_quantity < stock_minimum";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding low stocks: " + e.getMessage());
        }
        
        return stocks;
    }
    
    public List<Stock> findOverStocks() {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT * FROM stock WHERE available_quantity > stock_maximum";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                stocks.add(mapResultSetToStock(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding over stocks: " + e.getMessage());
        }
        
        return stocks;
    }
    
    public List<Stock> findStocksByProductId(int productId) {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT * FROM stock WHERE product_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, productId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stocks.add(mapResultSetToStock(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding stocks by product ID: " + e.getMessage());
        }
        
        return stocks;
    }
    
    private Stock mapResultSetToStock(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        
        stock.setId(rs.getInt("id"));
        
        int productId = rs.getInt("product_id");
        try {
            Product product = productService.getById(productId);
            stock.setProduct(product);
        } catch (SQLException e) {
            System.err.println("Error getting product by ID: " + e.getMessage());
        }
        
        stock.setAvailableQuantity(rs.getInt("available_quantity"));
        stock.setStockMinimum(rs.getFloat("stock_minimum"));
        stock.setStockMaximum(rs.getFloat("stock_maximum"));
        
        Timestamp entryTimestamp = rs.getTimestamp("entry_date");
        if (entryTimestamp != null) {
            stock.setEntryDate(entryTimestamp.toLocalDateTime());
        }
        
        Timestamp exitTimestamp = rs.getTimestamp("exit_date");
        if (exitTimestamp != null) {
            stock.setExitDate(exitTimestamp.toLocalDateTime());
        }
        
        return stock;
    }
} 