
package com.foodordering.system.database;

import java.sql.Connection;
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

            // Safely migrate existing databases.
            // Never drop or recreate the orders table.
            addColumnIfMissing(
                    connection,
                    "orders",
                    "payment_method",
                    "TEXT"
            );

            addColumnIfMissing(
                    connection,
                    "orders",
                    "payment_status",
                    "TEXT DEFAULT 'PENDING'"
            );

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
     * Accept only simple SQL identifiers.
     */
    private static String validateIdentifier(String identifier)
            throws SQLException {

        if (identifier == null ||
                !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new SQLException("Invalid SQL identifier.");
        }

        return identifier;
    }

    /*
     * Adds a column only if it does not already exist.
     */
    private static void addColumnIfMissing(
            Connection connection,
            String tableName,
            String columnName,
            String columnDefinition) throws SQLException {

        String safeTable = validateIdentifier(tableName);
        String safeColumn = validateIdentifier(columnName);

        // Only permit definitions used by the existing migrations.
        if (!"TEXT".equals(columnDefinition)
                && !"TEXT DEFAULT 'PENDING'".equals(columnDefinition)) {
            throw new SQLException("Invalid column definition.");
        }

        if (!columnExists(connection, safeTable, safeColumn)) {

            String sql = "ALTER TABLE \"" + safeTable
                    + "\" ADD COLUMN \"" + safeColumn
                    + "\" " + columnDefinition;

            try (Statement statement =
                         connection.createStatement()) {
                statement.execute(sql);
            }

            System.out.println(
                    "Added column " + safeColumn
                            + " to table " + safeTable);
        }
    }

    /*
     * Checks whether a column exists using SQLite metadata.
     */
    private static boolean columnExists(
            Connection connection,
            String tableName,
            String columnName) throws SQLException {

        String safeTable = validateIdentifier(tableName);
        String safeColumn = validateIdentifier(columnName);

        String sql = "PRAGMA table_info(\"" + safeTable + "\")";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String existingColumn =
                        resultSet.getString("name");

                if (safeColumn.equalsIgnoreCase(existingColumn)) {
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

        if (hasRestaurantId) {
            return false;
        }

        return true;
    }
}
