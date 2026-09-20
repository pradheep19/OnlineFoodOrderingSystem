package com.foodordering.system.dao;

import org.springframework.stereotype.Repository;
import com.foodordering.system.database.DatabaseManager;
import com.foodordering.system.model.Customer;
import com.foodordering.system.model.LoyaltyTier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomerDAO {

    private Map<Integer, Customer> customers;

    public CustomerDAO() {
        customers = new HashMap<>();
        initializeCustomers();

        
    }

    private void initializeCustomers() {

        loadCustomersFromDatabase();

        if (customers.isEmpty()) {
            loadSampleCustomers();
            saveAllCustomersToDatabase();
        }
    }

    private void loadSampleCustomers() {

        customers.put(1, new Customer(
                1,
                "Pradheep",
                "9876543210",
                "Vellore",
                2.5,
                LoyaltyTier.GOLD,
                850
        ));

        customers.put(2, new Customer(
                2,
                "Yukesh",
                "9876501234",
                "Katpadi",
                5.0,
                LoyaltyTier.SILVER,
                420
        ));

        customers.put(3, new Customer(
                3,
                "Rahul",
                "9876512345",
                "Vellore",
                8.0,
                LoyaltyTier.PLATINUM,
                1500
        ));

        customers.put(4, new Customer(
                4,
                "Arun",
                "9876523456",
                "Chennai",
                12.0,
                LoyaltyTier.REGULAR,
                100
        ));
    }

    private void loadCustomersFromDatabase() {

        String sql = """
                SELECT customer_id,
                       name,
                       phone,
                       address,
                       distance_from_restaurant,
                       loyalty_tier,
                       loyalty_points
                FROM customers
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                int customerId = resultSet.getInt("customer_id");
                String name = resultSet.getString("name");
                String phoneNumber = resultSet.getString("phone");
                String address = resultSet.getString("address");

                double distance =
                        resultSet.getDouble("distance_from_restaurant");

                String loyaltyTierText =
                        resultSet.getString("loyalty_tier");

                double loyaltyPoints =
                        resultSet.getDouble("loyalty_points");

                LoyaltyTier loyaltyTier;

                try {
                    loyaltyTier = LoyaltyTier.valueOf(loyaltyTierText);
                } catch (Exception e) {
                    loyaltyTier = LoyaltyTier.REGULAR;
                }

                Customer customer = new Customer(
                        customerId,
                        name,
                        phoneNumber,
                        address,
                        distance,
                        loyaltyTier,
                        loyaltyPoints
                );

                customers.put(customerId, customer);
            }

        } catch (SQLException e) {
            System.err.println("Failed to load customers from SQLite.");
            e.printStackTrace();
        }
    }

    private void saveAllCustomersToDatabase() {

        String sql = """
                INSERT OR REPLACE INTO customers
                (
                    customer_id,
                    name,
                    phone,
                    address,
                    distance_from_restaurant,
                    loyalty_tier,
                    loyalty_points
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Customer customer : customers.values()) {

                statement.setInt(
                        1,
                        customer.getCustomerId()
                );

                statement.setString(
                        2,
                        customer.getName()
                );

                statement.setString(
                        3,
                        customer.getPhoneNumber()
                );

                statement.setString(
                        4,
                        customer.getAddress()
                );

                statement.setDouble(
                        5,
                        customer.getDistanceFromRestaurant()
                );

                statement.setString(
                        6,
                        customer.getLoyaltyTier().name()
                );

                statement.setDouble(
                        7,
                        customer.getLoyaltyPoints()
                );

                statement.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Failed to save customers to SQLite.");
            e.printStackTrace();
        }
    }

    public Customer findById(int customerId) {
        return customers.get(customerId);
    }

    public List<Customer> findAll() {
        return new ArrayList<>(customers.values());
    }

    public boolean addCustomer(Customer customer) {

        if (customers.containsKey(customer.getCustomerId())) {
            return false;
        }

        String sql = """
                INSERT INTO customers
                (
                    customer_id,
                    name,
                    phone,
                    address,
                    distance_from_restaurant,
                    loyalty_tier,
                    loyalty_points
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customer.getCustomerId());
            statement.setString(2, customer.getName());
            statement.setString(3, customer.getPhoneNumber());
            statement.setString(4, customer.getAddress());
            statement.setDouble(
                    5,
                    customer.getDistanceFromRestaurant()
            );
            statement.setString(
                    6,
                    customer.getLoyaltyTier().name()
            );
            statement.setDouble(
                    7,
                    customer.getLoyaltyPoints()
            );

            statement.executeUpdate();

            customers.put(customer.getCustomerId(), customer);

            return true;

        } catch (SQLException e) {
            System.err.println("Failed to add customer to SQLite.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Customer customer) {

        if (!customers.containsKey(customer.getCustomerId())) {
            return false;
        }

        String sql = """
                UPDATE customers
                SET name = ?,
                    phone = ?,
                    address = ?,
                    distance_from_restaurant = ?,
                    loyalty_tier = ?,
                    loyalty_points = ?
                WHERE customer_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhoneNumber());
            statement.setString(3, customer.getAddress());
            statement.setDouble(
                    4,
                    customer.getDistanceFromRestaurant()
            );
            statement.setString(
                    5,
                    customer.getLoyaltyTier().name()
            );
            statement.setDouble(
                    6,
                    customer.getLoyaltyPoints()
            );
            statement.setInt(
                    7,
                    customer.getCustomerId()
            );

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                return false;
            }

            customers.put(customer.getCustomerId(), customer);

            return true;

        } catch (SQLException e) {
            System.err.println("Failed to update customer in SQLite.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomer(int customerId) {

        if (!customers.containsKey(customerId)) {
            return false;
        }

        String sql = """
                DELETE FROM customers
                WHERE customer_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted == 0) {
                return false;
            }

            customers.remove(customerId);

            return true;

        } catch (SQLException e) {
            System.err.println("Failed to delete customer from SQLite.");
            e.printStackTrace();
            return false;
        }
    }

    public int getCustomerCount() {
        return customers.size();
    }
}