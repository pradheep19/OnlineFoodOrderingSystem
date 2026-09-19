package com.foodordering.system.dao;

import com.foodordering.system.model.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDAO {

    private Map<Integer, Order> orders;

    public OrderDAO() {
        orders = new HashMap<>();
    }

    /*
     * Saves a new order.
     */
    public boolean saveOrder(Order order) {

        if (order == null) {
            return false;
        }

        if (orders.containsKey(order.getOrderId())) {
            return false;
        }

        orders.put(order.getOrderId(), order);

        return true;
    }

    /*
     * Finds an order using its ID.
     */
    public Order findById(int orderId) {

        return orders.get(orderId);
    }

    /*
     * Returns all orders.
     */
    public List<Order> findAll() {

        return new ArrayList<>(orders.values());
    }

    /*
     * Updates an existing order.
     */
    public boolean updateOrder(Order order) {

        if (order == null) {
            return false;
        }

        if (!orders.containsKey(order.getOrderId())) {
            return false;
        }

        orders.put(order.getOrderId(), order);

        return true;
    }

    /*
     * Deletes an order.
     */
    public boolean deleteOrder(int orderId) {

        if (!orders.containsKey(orderId)) {
            return false;
        }

        orders.remove(orderId);

        return true;
    }

    /*
     * Returns the number of orders.
     */
    public int getOrderCount() {

        return orders.size();
    }
}