package com.foodordering.system.dao;

import com.foodordering.system.database.DatabaseManager;
import com.foodordering.system.model.MenuItem;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MenuDAO {

    private final Map<Integer, MenuItem> menuItems = new HashMap<>();

    public MenuDAO() {
        ensureSampleMenuData();
        loadMenuItems();
    }

    // =========================================================
    // ENSURE SAMPLE MENU DATA EXISTS IN SQLITE
    // =========================================================

    private void ensureSampleMenuData() {

        List<MenuItem> sampleItems = createSampleMenuItems();

        String insertSql = """
                INSERT OR IGNORE INTO menu_items
                (
                    item_id,
                    restaurant_id,
                    name,
                    category,
                    base_price,
                    available,
                    dynamic_pricing_exempt,
                    available_from,
                    available_to
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(insertSql)) {

            for (MenuItem item : sampleItems) {

                statement.setInt(1, item.getItemId());
                statement.setInt(2, item.getRestaurantId());
                statement.setString(3, item.getName());
                statement.setString(4, item.getCategory());
                statement.setDouble(5, item.getBasePrice());
                statement.setInt(6, item.isAvailable() ? 1 : 0);
                statement.setInt(
                        7,
                        item.isDynamicPricingExempt() ? 1 : 0
                );
                statement.setString(
                        8,
                        item.getAvailableFrom().toString()
                );
                statement.setString(
                        9,
                        item.getAvailableTo().toString()
                );

                statement.executeUpdate();
            }

            /*
             * Earlier SQLite data incorrectly associated
             * Ice Cream with Spice Garden.
             *
             * The original MenuDAO shows that Ice Cream
             * belongs to Burger Hub (Restaurant ID 3).
             */
            String correctIceCreamRestaurant = """
                    UPDATE menu_items
                    SET restaurant_id = 3
                    WHERE item_id = 105
                    """;

            try (PreparedStatement updateStatement =
                         connection.prepareStatement(
                                 correctIceCreamRestaurant)) {

                updateStatement.executeUpdate();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Failed to initialize sample menu data."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // SAMPLE MENU
    // =========================================================

    private List<MenuItem> createSampleMenuItems() {

        List<MenuItem> items = new ArrayList<>();

        // =====================================================
        // SPICE GARDEN - RESTAURANT ID 1
        // =====================================================

        items.add(new MenuItem(
                101,
                1,
                "Chicken Biryani",
                "Main Course",
                220.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        ));

        items.add(new MenuItem(
                106,
                1,
                "Paneer Butter Masala",
                "Main Course",
                180.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        ));

        items.add(new MenuItem(
                107,
                1,
                "Veg Meals",
                "Main Course",
                150.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(21, 30)
        ));

        // =====================================================
        // PIZZA PALACE - RESTAURANT ID 2
        // =====================================================

        items.add(new MenuItem(
                102,
                2,
                "Paneer Pizza",
                "Pizza",
                280.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        ));

        items.add(new MenuItem(
                108,
                2,
                "Margherita Pizza",
                "Pizza",
                250.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        ));

        items.add(new MenuItem(
                109,
                2,
                "Garlic Bread",
                "Sides",
                120.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(23, 0)
        ));

        // =====================================================
        // BURGER HUB - RESTAURANT ID 3
        // =====================================================

        items.add(new MenuItem(
                103,
                3,
                "Veg Burger",
                "Fast Food",
                150.0,
                true,
                false,
                LocalTime.of(10, 0),
                LocalTime.of(22, 30)
        ));

        items.add(new MenuItem(
                104,
                3,
                "French Fries",
                "Fast Food",
                100.0,
                true,
                false,
                LocalTime.of(10, 0),
                LocalTime.of(22, 30)
        ));

        items.add(new MenuItem(
                105,
                3,
                "Ice Cream",
                "Dessert",
                90.0,
                true,
                true,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        ));

        return items;
    }

    // =========================================================
    // LOAD MENU ITEMS FROM SQLITE
    // =========================================================

    private void loadMenuItems() {

        String sql = """
                SELECT
                    item_id,
                    restaurant_id,
                    name,
                    category,
                    base_price,
                    available,
                    dynamic_pricing_exempt,
                    available_from,
                    available_to
                FROM menu_items
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            menuItems.clear();

            while (resultSet.next()) {

                MenuItem item = new MenuItem(
                        resultSet.getInt("item_id"),
                        resultSet.getInt("restaurant_id"),
                        resultSet.getString("name"),
                        resultSet.getString("category"),
                        resultSet.getDouble("base_price"),
                        resultSet.getInt("available") == 1,
                        resultSet.getInt("dynamic_pricing_exempt") == 1,
                        LocalTime.parse(
                                resultSet.getString("available_from")
                        ),
                        LocalTime.parse(
                                resultSet.getString("available_to")
                        )
                );

                menuItems.put(item.getItemId(), item);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Failed to load menu items from SQLite."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // FIND ITEM BY ID
    // =========================================================

    public MenuItem findById(int itemId) {
        return menuItems.get(itemId);
    }

    // =========================================================
    // FIND ALL ITEMS
    // =========================================================

    public List<MenuItem> findAll() {
        return new ArrayList<>(menuItems.values());
    }

    // =========================================================
    // FIND AVAILABLE ITEMS
    // =========================================================

    public List<MenuItem> findAvailableItems() {

        return menuItems.values()
                .stream()
                .filter(MenuItem::isAvailable)
                .toList();
    }

    // =========================================================
    // FIND ITEMS BY RESTAURANT
    // =========================================================

    public List<MenuItem> findByRestaurantId(int restaurantId) {

        return menuItems.values()
                .stream()
                .filter(item ->
                        item.getRestaurantId() == restaurantId
                )
                .toList();
    }

    // =========================================================
    // FIND AVAILABLE ITEMS BY RESTAURANT
    // =========================================================

    public List<MenuItem> findAvailableItemsByRestaurantId(
            int restaurantId) {

        return menuItems.values()
                .stream()
                .filter(item ->
                        item.getRestaurantId() == restaurantId
                        && item.isAvailable()
                )
                .toList();
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    public void add(MenuItem menuItem) {

        menuItems.put(
                menuItem.getItemId(),
                menuItem
        );

        saveToDatabase(menuItem);
    }

    // =========================================================
    // UPDATE ITEM
    // =========================================================

    public void update(MenuItem menuItem) {

        menuItems.put(
                menuItem.getItemId(),
                menuItem
        );

        saveToDatabase(menuItem);
    }

    // =========================================================
    // DELETE ITEM
    // =========================================================

    public void delete(int itemId) {

        menuItems.remove(itemId);

        String sql = """
                DELETE FROM menu_items
                WHERE item_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, itemId);
            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Failed to delete menu item from SQLite."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // SAVE / UPDATE ITEM IN SQLITE
    // =========================================================

    private void saveToDatabase(MenuItem item) {

        String sql = """
                INSERT OR REPLACE INTO menu_items
                (
                    item_id,
                    restaurant_id,
                    name,
                    category,
                    base_price,
                    available,
                    dynamic_pricing_exempt,
                    available_from,
                    available_to
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, item.getItemId());
            statement.setInt(2, item.getRestaurantId());
            statement.setString(3, item.getName());
            statement.setString(4, item.getCategory());
            statement.setDouble(5, item.getBasePrice());
            statement.setInt(
                    6,
                    item.isAvailable() ? 1 : 0
            );
            statement.setInt(
                    7,
                    item.isDynamicPricingExempt() ? 1 : 0
            );
            statement.setString(
                    8,
                    item.getAvailableFrom().toString()
            );
            statement.setString(
                    9,
                    item.getAvailableTo().toString()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                    "Failed to save menu item to SQLite."
            );

            e.printStackTrace();
        }
    }

    // =========================================================
    // TOTAL MENU SIZE
    // =========================================================

    public int size() {
        return menuItems.size();
    }
}