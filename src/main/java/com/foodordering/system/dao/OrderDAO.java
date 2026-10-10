
package com.foodordering.system.dao;

import com.foodordering.system.database.DatabaseManager;
import com.foodordering.system.model.Coupon;
import com.foodordering.system.model.MenuItem;
import com.foodordering.system.model.Order;
import com.foodordering.system.model.OrderItem;
import com.foodordering.system.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDAO {

    private final Map<Integer, Order> orders = new HashMap<>();

    private final CustomerDAO customerDAO;
    private final RestaurantDAO restaurantDAO;
    private final MenuDAO menuDAO;

    @Autowired
    public OrderDAO(
            CustomerDAO customerDAO,
            RestaurantDAO restaurantDAO,
            MenuDAO menuDAO) {

        this.customerDAO = customerDAO;
        this.restaurantDAO = restaurantDAO;
        this.menuDAO = menuDAO;

        loadOrdersFromDatabase();
    }

    public OrderDAO() {
        this.customerDAO = new CustomerDAO();
        this.restaurantDAO = new RestaurantDAO();
        this.menuDAO = new MenuDAO();

        loadOrdersFromDatabase();
    }

    // =========================================================
    // SAVE NEW ORDER
    // =========================================================

    public boolean saveOrder(Order order) {

        if (order == null || orders.containsKey(order.getOrderId())) {
            return false;
        }

        String orderSql = """
                INSERT INTO orders (
                    order_id,
                    customer_id,
                    restaurant_id,
                    subtotal,
                    dynamic_pricing_amount,
                    discount_amount,
                    delivery_fee,
                    final_amount,
                    coupon_code,
                    status,
                    order_date_time,
                    payment_method,
                    payment_status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String itemSql = """
                INSERT INTO order_items (
                    order_id,
                    item_id,
                    quantity,
                    item_price,
                    total_price
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement orderStatement =
                         connection.prepareStatement(orderSql);
                 PreparedStatement itemStatement =
                         connection.prepareStatement(itemSql)) {

                setOrderStatementValues(orderStatement, order);
                orderStatement.executeUpdate();

                saveOrderItems(itemStatement, order);

                connection.commit();
                orders.put(order.getOrderId(), order);

                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Failed to save order. Transaction rolled back.");
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database connection failed while saving order.");
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // FIND ORDER BY ID
    // =========================================================

    public Order findById(int orderId) {
        return orders.get(orderId);
    }

    // =========================================================
    // FIND ALL ORDERS
    // =========================================================

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    // =========================================================
    // UPDATE EXISTING ORDER
    // =========================================================

    public boolean updateOrder(Order order) {

        if (order == null || !orders.containsKey(order.getOrderId())) {
            return false;
        }

        String updateOrderSql = """
                UPDATE orders
                SET
                    customer_id = ?,
                    restaurant_id = ?,
                    subtotal = ?,
                    dynamic_pricing_amount = ?,
                    discount_amount = ?,
                    delivery_fee = ?,
                    final_amount = ?,
                    coupon_code = ?,
                    status = ?,
                    order_date_time = ?,
                    payment_method = ?,
                    payment_status = ?
                WHERE order_id = ?
                """;

        String deleteItemsSql = """
                DELETE FROM order_items
                WHERE order_id = ?
                """;

        String insertItemSql = """
                INSERT INTO order_items (
                    order_id,
                    item_id,
                    quantity,
                    item_price,
                    total_price
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement updateStatement =
                         connection.prepareStatement(updateOrderSql);
                 PreparedStatement deleteItemsStatement =
                         connection.prepareStatement(deleteItemsSql);
                 PreparedStatement insertItemStatement =
                         connection.prepareStatement(insertItemSql)) {

                setUpdateOrderStatementValues(updateStatement, order);

                int updatedRows = updateStatement.executeUpdate();

                if (updatedRows == 0) {
                    connection.rollback();
                    return false;
                }

                deleteItemsStatement.setInt(1, order.getOrderId());
                deleteItemsStatement.executeUpdate();

                saveOrderItems(insertItemStatement, order);

                connection.commit();
                orders.put(order.getOrderId(), order);

                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Failed to update order. Transaction rolled back.");
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database connection failed while updating order.");
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // DELETE ORDER
    // =========================================================

    public boolean deleteOrder(int orderId) {

        if (!orders.containsKey(orderId)) {
            return false;
        }

        String deleteItemsSql = """
                DELETE FROM order_items
                WHERE order_id = ?
                """;

        String deleteOrderSql = """
                DELETE FROM orders
                WHERE order_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement deleteItemsStatement =
                         connection.prepareStatement(deleteItemsSql);
                 PreparedStatement deleteOrderStatement =
                         connection.prepareStatement(deleteOrderSql)) {

                deleteItemsStatement.setInt(1, orderId);
                deleteItemsStatement.executeUpdate();

                deleteOrderStatement.setInt(1, orderId);

                int deletedRows = deleteOrderStatement.executeUpdate();

                if (deletedRows == 0) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                orders.remove(orderId);

                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Failed to delete order. Transaction rolled back.");
                e.printStackTrace();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database connection failed while deleting order.");
            e.printStackTrace();
            return false;
        }
    }

    // =========================================================
    // GET ORDER COUNT
    // =========================================================

    public int getOrderCount() {

        String sql = "SELECT COUNT(*) FROM orders";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Failed to retrieve order count.");
            e.printStackTrace();
        }

        return 0;
    }

    // =========================================================
    // LOAD ALL ORDERS FROM SQLITE
    // =========================================================

    private void loadOrdersFromDatabase() {

        String sql = """
                SELECT
                    order_id,
                    customer_id,
                    restaurant_id,
                    subtotal,
                    dynamic_pricing_amount,
                    discount_amount,
                    delivery_fee,
                    final_amount,
                    coupon_code,
                    status,
                    order_date_time,
                    payment_method,
                    payment_status
                FROM orders
                ORDER BY order_id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            orders.clear();

            while (resultSet.next()) {

                Order order = createOrderFromResultSet(resultSet);

                if (order != null) {
                    loadOrderItems(connection, order);
                    orders.put(order.getOrderId(), order);
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to load orders from SQLite.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // CREATE ORDER FROM DATABASE RECORD
    // =========================================================

    private Order createOrderFromResultSet(
            ResultSet resultSet) throws SQLException {

        int orderId = resultSet.getInt("order_id");
        int customerId = resultSet.getInt("customer_id");
        int restaurantId = resultSet.getInt("restaurant_id");

        var customer = customerDAO.findById(customerId);
        var restaurant = restaurantDAO.findById(restaurantId);

        if (customer == null || restaurant == null) {
            System.err.println(
                    "Skipping order " + orderId
                            + " because its customer or restaurant was not found."
            );
            return null;
        }

        Order order = new Order(orderId, customer, restaurant);

        order.setSubtotal(resultSet.getDouble("subtotal"));
        order.setDynamicPricingAmount(
                resultSet.getDouble("dynamic_pricing_amount"));
        order.setDiscountAmount(resultSet.getDouble("discount_amount"));
        order.setDeliveryFee(resultSet.getDouble("delivery_fee"));
        order.setFinalAmount(resultSet.getDouble("final_amount"));

        String status = resultSet.getString("status");

        if (status != null && !status.isBlank()) {
            order.setStatus(OrderStatus.valueOf(status));
        }

        String dateTime = resultSet.getString("order_date_time");

        if (dateTime != null && !dateTime.isBlank()) {
            order.setOrderDateTime(LocalDateTime.parse(dateTime));
        }

        String paymentMethod = resultSet.getString("payment_method");
        String paymentStatus = resultSet.getString("payment_status");

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(
                paymentStatus != null && !paymentStatus.isBlank()
                        ? paymentStatus
                        : "PENDING"
        );

        // The coupon code is stored in SQLite. The saved financial
        // values above are sufficient to display a completed order.
        return order;
    }

    // =========================================================
    // LOAD ORDER ITEMS
    // =========================================================

    private void loadOrderItems(
            Connection connection,
            Order order) throws SQLException {

        String sql = """
                SELECT
                    item_id,
                    quantity,
                    item_price,
                    total_price
                FROM order_items
                WHERE order_id = ?
                ORDER BY order_item_id
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, order.getOrderId());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int itemId = resultSet.getInt("item_id");
                    int quantity = resultSet.getInt("quantity");
                    double savedItemPrice = resultSet.getDouble("item_price");
                    double savedTotalPrice = resultSet.getDouble("total_price");

                    MenuItem menuItem = menuDAO.findById(itemId);

                    if (menuItem == null) {
                        System.err.println(
                                "Skipping order item " + itemId
                                        + " because the menu item was not found."
                        );
                        continue;
                    }

                    OrderItem orderItem = new OrderItem(menuItem, quantity);

                    /*
                     * Restore the price saved when the order was placed.
                     * This prevents the current menu base price from
                     * replacing the historical order price.
                     */
                    if (quantity > 0) {
                        if (Math.abs(savedTotalPrice
                                - (savedItemPrice * quantity)) < 0.01) {
                            orderItem.setItemPrice(savedItemPrice);
                        } else {
                            orderItem.setItemPrice(
                                    savedTotalPrice / quantity);
                        }
                    } else {
                        orderItem.setItemPrice(savedItemPrice);
                    }

                    order.addItem(orderItem);
                }
            }
        }
    }

    // =========================================================
    // SAVE ORDER ITEMS
    // =========================================================

    private void saveOrderItems(
            PreparedStatement statement,
            Order order) throws SQLException {

        for (OrderItem item : order.getItems()) {

            statement.setInt(1, order.getOrderId());
            statement.setInt(2, item.getMenuItem().getItemId());
            statement.setInt(3, item.getQuantity());
            statement.setDouble(4, item.getItemPrice());
            statement.setDouble(5, item.getTotalPrice());

            statement.addBatch();
        }

        statement.executeBatch();
    }

    // =========================================================
    // SET INSERT VALUES
    // =========================================================

    private void setOrderStatementValues(
            PreparedStatement statement,
            Order order) throws SQLException {

        statement.setInt(1, order.getOrderId());
        statement.setInt(2, order.getCustomer().getCustomerId());
        statement.setInt(3, order.getRestaurant().getRestaurantId());
        statement.setDouble(4, order.getSubtotal());
        statement.setDouble(5, order.getDynamicPricingAmount());
        statement.setDouble(6, order.getDiscountAmount());
        statement.setDouble(7, order.getDeliveryFee());
        statement.setDouble(8, order.getFinalAmount());

        setCouponCode(statement, 9, order);

        statement.setString(
                10,
                order.getStatus() != null
                        ? order.getStatus().name()
                        : null
        );

        statement.setString(
                11,
                order.getOrderDateTime() != null
                        ? order.getOrderDateTime().toString()
                        : null
        );

        statement.setString(12, order.getPaymentMethod());

        statement.setString(
                13,
                order.getPaymentStatus() != null
                        ? order.getPaymentStatus()
                        : "PENDING"
        );
    }

    // =========================================================
    // SET UPDATE VALUES
    // =========================================================

    private void setUpdateOrderStatementValues(
            PreparedStatement statement,
            Order order) throws SQLException {

        statement.setInt(1, order.getCustomer().getCustomerId());
        statement.setInt(2, order.getRestaurant().getRestaurantId());
        statement.setDouble(3, order.getSubtotal());
        statement.setDouble(4, order.getDynamicPricingAmount());
        statement.setDouble(5, order.getDiscountAmount());
        statement.setDouble(6, order.getDeliveryFee());
        statement.setDouble(7, order.getFinalAmount());

        setCouponCode(statement, 8, order);

        statement.setString(
                9,
                order.getStatus() != null
                        ? order.getStatus().name()
                        : null
        );

        statement.setString(
                10,
                order.getOrderDateTime() != null
                        ? order.getOrderDateTime().toString()
                        : null
        );

        statement.setString(11, order.getPaymentMethod());

        statement.setString(
                12,
                order.getPaymentStatus() != null
                        ? order.getPaymentStatus()
                        : "PENDING"
        );

        statement.setInt(13, order.getOrderId());
    }

    // =========================================================
    // SET COUPON CODE
    // =========================================================

    private void setCouponCode(
            PreparedStatement statement,
            int parameterIndex,
            Order order) throws SQLException {

        Coupon coupon = order.getAppliedCoupon();

        if (coupon == null) {
            statement.setString(parameterIndex, null);
            return;
        }

        statement.setString(
                parameterIndex,
                coupon.getCouponCode()
        );
    }
}