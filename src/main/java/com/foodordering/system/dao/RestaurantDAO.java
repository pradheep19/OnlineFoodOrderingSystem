package com.foodordering.system.dao;

import com.foodordering.system.database.DatabaseManager;
import com.foodordering.system.model.Restaurant;
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
public class RestaurantDAO {

    private Map<Integer, Restaurant> restaurants;

    public RestaurantDAO() {
        restaurants = new HashMap<>();
        initializeRestaurants();
      
    }

    private void initializeRestaurants() {

        loadRestaurantsFromDatabase();

        if (restaurants.isEmpty()) {
            loadSampleRestaurants();
            saveAllRestaurantsToDatabase();
        }
    }

    // Load restaurants from SQLite
    private void loadRestaurantsFromDatabase() {

        String sql = """
                SELECT restaurant_id, name, address,
                       opening_time, closing_time,
                       delivery_radius, minimum_order_value
                FROM restaurants
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                int restaurantId = resultSet.getInt("restaurant_id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");

                LocalTime openingTime =
                        LocalTime.parse(resultSet.getString("opening_time"));

                LocalTime closingTime =
                        LocalTime.parse(resultSet.getString("closing_time"));

                double deliveryRadius =
                        resultSet.getDouble("delivery_radius");

                double minimumOrderValue =
                        resultSet.getDouble("minimum_order_value");

                Restaurant restaurant = new Restaurant(
                        restaurantId,
                        name,
                        address,
                        openingTime,
                        closingTime,
                        deliveryRadius,
                        minimumOrderValue
                );

                restaurants.put(restaurantId, restaurant);
            }

        } catch (SQLException | RuntimeException e) {
            System.err.println("Failed to load restaurants from SQLite.");
            e.printStackTrace();
        }
    }

    // Save all restaurants to SQLite
    private void saveAllRestaurantsToDatabase() {

        String sql = """
                INSERT OR REPLACE INTO restaurants
                (restaurant_id, name, address, opening_time,
                 closing_time, delivery_radius, minimum_order_value)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Restaurant restaurant : restaurants.values()) {

                statement.setInt(1, restaurant.getRestaurantId());
                statement.setString(2, restaurant.getName());
                statement.setString(3, restaurant.getAddress());

                statement.setString(
                        4,
                        restaurant.getOpeningTime().toString()
                );

                statement.setString(
                        5,
                        restaurant.getClosingTime().toString()
                );

                statement.setDouble(
                        6,
                        restaurant.getDeliveryRadius()
                );

                statement.setDouble(
                        7,
                        restaurant.getMinimumOrderValue()
                );

                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException e) {
            System.err.println("Failed to save restaurants to SQLite.");
            e.printStackTrace();
        }
    }

    // Existing sample restaurant data
    private void loadSampleRestaurants() {

        restaurants.put(
                1,
                new Restaurant(
                        1,
                        "Spice Garden",
                        "Vellore",
                        LocalTime.of(10, 0),
                        LocalTime.of(23, 0),
                        10.0,
                        100.0
                )
        );

        restaurants.put(
                2,
                new Restaurant(
                        2,
                        "Pizza Palace",
                        "Katpadi",
                        LocalTime.of(11, 0),
                        LocalTime.of(23, 30),
                        8.0,
                        150.0
                )
        );

        restaurants.put(
                3,
                new Restaurant(
                        3,
                        "Burger Hub",
                        "Vellore",
                        LocalTime.of(9, 0),
                        LocalTime.of(22, 30),
                        6.0,
                        80.0
                )
        );
    }

    // Find restaurant by ID
    public Restaurant findById(int restaurantId) {
        return restaurants.get(restaurantId);
    }

    // Return all restaurants
    public List<Restaurant> findAll() {
        return new ArrayList<>(restaurants.values());
    }

    // Add a new restaurant
    public boolean addRestaurant(Restaurant restaurant) {

        if (restaurants.containsKey(restaurant.getRestaurantId())) {
            return false;
        }

        restaurants.put(
                restaurant.getRestaurantId(),
                restaurant
        );

        saveRestaurantToDatabase(restaurant);

        return true;
    }

    // Update an existing restaurant
    public boolean updateRestaurant(Restaurant restaurant) {

        if (!restaurants.containsKey(restaurant.getRestaurantId())) {
            return false;
        }

        restaurants.put(
                restaurant.getRestaurantId(),
                restaurant
        );

        saveRestaurantToDatabase(restaurant);

        return true;
    }

    // Delete restaurant
    public boolean deleteRestaurant(int restaurantId) {

        if (!restaurants.containsKey(restaurantId)) {
            return false;
        }

        restaurants.remove(restaurantId);

        deleteRestaurantFromDatabase(restaurantId);

        return true;
    }

    // Save one restaurant to SQLite
    private void saveRestaurantToDatabase(Restaurant restaurant) {

        String sql = """
                INSERT OR REPLACE INTO restaurants
                (restaurant_id, name, address, opening_time,
                 closing_time, delivery_radius, minimum_order_value)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, restaurant.getRestaurantId());
            statement.setString(2, restaurant.getName());
            statement.setString(3, restaurant.getAddress());

            statement.setString(
                    4,
                    restaurant.getOpeningTime().toString()
            );

            statement.setString(
                    5,
                    restaurant.getClosingTime().toString()
            );

            statement.setDouble(
                    6,
                    restaurant.getDeliveryRadius()
            );

            statement.setDouble(
                    7,
                    restaurant.getMinimumOrderValue()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save restaurant to SQLite.");
            e.printStackTrace();
        }
    }

    // Delete one restaurant from SQLite
   private void deleteRestaurantFromDatabase(int restaurantId) {

    String sql =
            "DELETE FROM restaurants WHERE restaurant_id = ?";

    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, restaurantId);

        statement.executeUpdate();

    } catch (SQLException e) {
        System.err.println("Failed to delete restaurant from SQLite.");
        e.printStackTrace();
    }
}

    // Number of restaurants
    public int getRestaurantCount() {
        return restaurants.size();
    }
}