package com.foodordering.system.dao;

import org.springframework.stereotype.Repository;
import com.foodordering.system.model.Customer;
import com.foodordering.system.model.LoyaltyTier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Repository
public class CustomerDAO {

    private Map<Integer, Customer> customers;

    public CustomerDAO() {
        customers = new HashMap<>();
        loadSampleCustomers();
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

        customers.put(customer.getCustomerId(), customer);
        return true;
    }

    public boolean updateCustomer(Customer customer) {

        if (!customers.containsKey(customer.getCustomerId())) {
            return false;
        }

        customers.put(customer.getCustomerId(), customer);
        return true;
    }

    public boolean deleteCustomer(int customerId) {

        if (!customers.containsKey(customerId)) {
            return false;
        }

        customers.remove(customerId);
        return true;
    }

    public int getCustomerCount() {
        return customers.size();
    }
}