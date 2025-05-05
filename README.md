# Fruitables Management System

A JavaFX application for managing agricultural products, their types, and stock levels with a modern UI.

## Features

- **Beautiful Modern Interface**: Clean, responsive UI with FontAwesome icons and material design elements
- **Product Management**: Add, edit, delete, and search products
- **Product Type Management**: Manage seasonal product types and production methods
- **Stock Management**: Control inventory levels and get alerts on low stock
- **Reports & Analytics**: Generate reports and visualize data with charts

## Setup Instructions

### Database Setup

1. Make sure MySQL server is running
2. Execute the SQL script located at `src/main/resources/database.sql`
   ```
   mysql -u root -p < src/main/resources/database.sql
   ```
   This will create the database `reap` along with the necessary tables and sample data.

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Build and run the project using Maven:
   ```
   mvn clean javafx:run
   ```

Alternatively, you can run the project from your IDE by executing the `app.FruitablesApp` class.

## Project Structure

- **Models**: Product, ProductType, Stock
- **Services**: ProductService, ProductTypeService, StockService
- **Controllers**: Dashboard, ProductManagement, ProductTypeManagement, StockManagement, Reports
- **Views**: FXML files for the UI
- **Utils**: Database connection utility
- **Styles**: CSS stylesheets for theming

## Dependencies

- JavaFX 17+
- MySQL Connector/J 8.0+
- Ikonli (FontAwesome 5) for icons

## Screenshots

(Screenshots would be inserted here)

## License

MIT License
