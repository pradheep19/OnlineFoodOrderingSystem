
package com.foodordering.system.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL =
            "jdbc:sqlite:foodordering.db";

    private DatabaseManager() {
        // Utility class
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {

        String createCustomersTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    phone TEXT,
                    address TEXT,
                    distance_from_restaurant REAL,
                    loyalty_tier TEXT,
                    loyalty_points REAL
                )
                """;

        String createRestaurantsTable = """
                CREATE TABLE IF NOT EXISTS restaurants (
                    restaurant_id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    address TEXT,
                    opening_time TEXT,
                    closing_time TEXT,
                    delivery_radius REAL,
                    minimum_order_value REAL
                )
                """;

        String createMenuItemsTable = """
                CREATE TABLE IF NOT EXISTS menu_items (
                    item_id INTEGER PRIMARY KEY,
                    restaurant_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT,
                    base_price REAL NOT NULL,
                    available INTEGER,
                    dynamic_pricing_exempt INTEGER,
                    available_from TEXT,
                    available_to TEXT,
                    FOREIGN KEY (restaurant_id)
                        REFERENCES restaurants(restaurant_id)
                )
                """;

        String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS orders (
                    order_id INTEGER PRIMARY KEY,
                    customer_id INTEGER NOT NULL,
                    restaurant_id INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    dynamic_pricing_amount REAL NOT NULL,
                    discount_amount REAL NOT NULL,
                    delivery_fee REAL NOT NULL,
                    final_amount REAL NOT NULL,
                    coupon_code TEXT,
                    status TEXT,
                    order_date_time TEXT,
                    payment_method TEXT,
                    payment_status TEXT DEFAULT 'PENDING',
                    FOREIGN KEY (customer_id)
                        REFERENCES customers(customer_id),
                    FOREIGN KEY (restaurant_id)
                        REFERENCES restaurants(restaurant_id)
                )
                """;

        String createOrderItemsTable = """
                CREATE TABLE IF NOT EXISTS order_items (
                    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    item_price REAL NOT NULL,
                    total_price REAL NOT NULL,
                    FOREIGN KEY (order_id)
                        REFERENCES orders(order_id),
                    FOREIGN KEY (item_id)
                        REFERENCES menu_items(item_id)
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createCustomersTable);
            statement.execute(createRestaurantsTable);

            // Preserve the existing menu migration behavior.
            if (menuItemsTableNeedsMigration(connection)) {
                statement.execute(
                        "DROP TABLE IF EXISTS menu_items");
            }

            statement.execute(createMenuItemsTable);
            statement.execute(createOrdersTable);

            // Use fixed SQL for the supported payment migrations.
            addPaymentMethodColumnIfMissing(connection);
            addPaymentStatusColumnIfMissing(connection);

            statement.execute(createOrderItemsTable);

            System.out.println(
                    "SQLite database initialized successfully.");

        } catch (SQLException e) {
            System.err.println(
                    "Database initialization failed.");
            e.printStackTrace();
        }
    }

    /*
     * Adds payment_method only if it does not exist.
     */
    private static void addPaymentMethodColumnIfMissing(
            Connection connection) throws SQLException {

        if (!columnExists(
                connection, "orders", "payment_method")) {

            try (Statement statement =
                         connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE orders ADD COLUMN payment_method TEXT");
            }

            System.out.println(
                    "Added column payment_method to table orders");
        }
    }

    /*
     * Adds payment_status only if it does not exist.
     */
    private static void addPaymentStatusColumnIfMissing(
            Connection connection) throws SQLException {

        if (!columnExists(
                connection, "orders", "payment_status")) {

            try (Statement statement =
                         connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE orders ADD COLUMN payment_status "
                                + "TEXT DEFAULT 'PENDING'");
            }

            System.out.println(
                    "Added column payment_status to table orders");
        }
    }

    /*
     * Checks whether a column exists using JDBC metadata.
     */
    private static boolean columnExists(
            Connection connection,
            String tableName,
            String columnName) throws SQLException {

        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet columns = metadata.getColumns(
                null, null, tableName, null)) {

            while (columns.next()) {
                String existingColumn =
                        columns.getString("COLUMN_NAME");

                if (columnName.equalsIgnoreCase(existingColumn)) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Checks whether menu_items exists and contains restaurant_id.
     */
    private static boolean menuItemsTableNeedsMigration(
            Connection connection) throws SQLException {

        String sql = "PRAGMA table_info(menu_items)";

        boolean tableExists = false;
        boolean hasRestaurantId = false;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                tableExists = true;

                String columnName =
                        resultSet.getString("name");

                if ("restaurant_id".equalsIgnoreCase(columnName)) {
                    hasRestaurantId = true;
                }
            }
        }

        if (!tableExists) {
            return false;
        }

        return !hasRestaurantId;
    }
}
