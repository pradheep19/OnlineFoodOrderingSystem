package com.foodordering.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:foodordering.db";

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

            // Create customers and restaurants first
            statement.execute(createCustomersTable);
            statement.execute(createRestaurantsTable);

            /*
             * The original menu_items table did not contain restaurant_id.
             * Since the current menu_items table is empty, recreate only
             * this table with the correct schema.
             */
            if (menuItemsTableNeedsMigration(connection)) {

                statement.execute("DROP TABLE IF EXISTS menu_items");
            }

            // Create the corrected menu_items table
            statement.execute(createMenuItemsTable);

            // Create orders and order_items
            statement.execute(createOrdersTable);
            statement.execute(createOrderItemsTable);

        } catch (SQLException e) {

            System.err.println(
                    "Database initialization failed."
            );

            e.printStackTrace();
        }
    }

    /*
     * Checks whether menu_items exists and whether it contains
     * the restaurant_id column.
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

        /*
         * No table yet:
         * Nothing needs to be migrated.
         */
        if (!tableExists) {
            return false;
        }

        /*
         * Table already has restaurant_id:
         * Keep it.
         */
        if (hasRestaurantId) {
            return false;
        }

        /*
         * Existing old schema does not contain restaurant_id.
         * At this stage of development the table should be empty.
         */
        return true;
    }
}