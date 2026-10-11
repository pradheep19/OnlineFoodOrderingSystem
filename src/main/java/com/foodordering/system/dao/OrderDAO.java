package com.foodordering.system.dao;

import com.foodordering.system.database.DatabaseManager;

import com.foodordering.system.model.Coupon;

import com.foodordering.system.model.MenuItem;

import com.foodordering.system.model.Order;

import com.foodordering.system.model.OrderItem;

import com.foodordering.system.model.OrderStatus;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

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

    private static final Logger logger =

            LoggerFactory.getLogger(OrderDAO.class);

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

    // SAVE NEW ORDER

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

                rollbackTransaction(connection, e);

                logger.error(

                        "Failed to save order {}.",

                        order.getOrderId(), e);

                return false;

            }

        } catch (SQLException e) {

            logger.error(

                    "Database connection failed while saving order {}.",

                    order.getOrderId(), e);

            return false;

        }

    }

    // FIND ORDER BY ID

    public Order findById(int orderId) {

        return orders.get(orderId);

    }

    // FIND ALL ORDERS

    public List<Order> findAll() {

        return new ArrayList<>(orders.values());

    }

    // UPDATE EXISTING ORDER

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

                rollbackTransaction(connection, e);

                logger.error(

                        "Failed to update order {}.",

                        order.getOrderId(), e);

                return false;

            }

        } catch (SQLException e) {

            logger.error(

                    "Database connection failed while updating order {}.",

                    order.getOrderId(), e);

            return false;

        }

    }

    // DELETE ORDER

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

                rollbackTransaction(connection, e);

                logger.error("Failed to delete order {}.", orderId, e);

                return false;

            }

        } catch (SQLException e) {

            logger.error(

                    "Database connection failed while deleting order {}.",

                    orderId, e);

            return false;

        }

    }

    // ROLLBACK TRANSACTION

    private void rollbackTransaction(

            Connection connection,

            SQLException originalException) {

        try {

            connection.rollback();

        } catch (SQLException rollbackException) {

            originalException.addSuppressed(rollbackException);

            logger.error("Failed to roll back database transaction.",

                    rollbackException);

        }

    }

    // GET ORDER COUNT

    public int getOrderCount() {

        String sql = "SELECT COUNT(*) FROM orders";

        try (Connection connection = DatabaseManager.getConnection();

             PreparedStatement statement =

                     connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {

                return resultSet.getInt(1);

            }

        } catch (SQLException e) {

            logger.error("Failed to retrieve order count.", e);

        }

        return 0;

    }

    // LOAD ALL ORDERS FROM SQLITE

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

             PreparedStatement statement =

                     connection.prepareStatement(sql);

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

            logger.error("Failed to load orders from SQLite.", e);

        }

    }

    // CREATE ORDER FROM DATABASE RECORD

    private Order createOrderFromResultSet(

            ResultSet resultSet) throws SQLException {

        int orderId = resultSet.getInt("order_id");

        int customerId = resultSet.getInt("customer_id");

        int restaurantId = resultSet.getInt("restaurant_id");

        var customer = customerDAO.findById(customerId);

        var restaurant = restaurantDAO.findById(restaurantId);

        if (customer == null || restaurant == null) {

            logger.warn(

                    "Skipping order {} because its customer or restaurant was not found.",

                    orderId);

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

        // Saved financial values are retained for historical orders.

        // The coupon code remains stored in SQLite.

        return order;

    }

    // LOAD ORDER ITEMS

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

        try (PreparedStatement statement =

                     connection.prepareStatement(sql)) {

            statement.setInt(1, order.getOrderId());

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    int itemId = resultSet.getInt("item_id");

                    int quantity = resultSet.getInt("quantity");

                    double savedItemPrice =

                            resultSet.getDouble("item_price");

                    double savedTotalPrice =

                            resultSet.getDouble("total_price");

                    MenuItem menuItem = menuDAO.findById(itemId);

                    if (menuItem == null) {

                        logger.warn(

                                "Skipping order item {} because the menu item was not found.",

                                itemId);

                        continue;

                    }

                    OrderItem orderItem =

                            new OrderItem(menuItem, quantity);

                    // Restore the price saved when the order was placed.

                    if (quantity > 0) {

                        if (Math.abs(

                                savedTotalPrice -

                                        (savedItemPrice * quantity)

                        ) < 0.01) {

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

    // SAVE ORDER ITEMS

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

    // SET INSERT VALUES

    private void setOrderStatementValues(
            PreparedStatement statement,
            Order order) throws SQLException {

        statement.setInt(1, order.getOrderId());
        statement.setInt(2, order.getCustomer().getCustomerId());
        statement.setInt(3, order.getRestaurant().getRestaurantId());

        setFinancialValues(statement, order, 4);
        setCouponCode(statement, 9, order);
        setOrderStatus(statement, 10, order);
        setOrderDateTime(statement, 11, order);

        statement.setString(12, order.getPaymentMethod());
        setPaymentStatus(statement, 13, order);
    }

    // SET UPDATE VALUES

    private void setUpdateOrderStatementValues(
            PreparedStatement statement,
            Order order) throws SQLException {

        statement.setInt(1, order.getCustomer().getCustomerId());
        statement.setInt(2, order.getRestaurant().getRestaurantId());

        setFinancialValues(statement, order, 3);
        setCouponCode(statement, 8, order);
        setOrderStatus(statement, 9, order);
        setOrderDateTime(statement, 10, order);

        statement.setString(11, order.getPaymentMethod());
        setPaymentStatus(statement, 12, order);

        statement.setInt(13, order.getOrderId());
    }

    // SET REUSABLE FINANCIAL VALUES

    private void setFinancialValues(
            PreparedStatement statement,
            Order order,
            int startIndex) throws SQLException {

        statement.setDouble(startIndex, order.getSubtotal());
        statement.setDouble(startIndex + 1, order.getDynamicPricingAmount());
        statement.setDouble(startIndex + 2, order.getDiscountAmount());
        statement.setDouble(startIndex + 3, order.getDeliveryFee());
        statement.setDouble(startIndex + 4, order.getFinalAmount());
    }

    // SET ORDER STATUS

    private void setOrderStatus(
            PreparedStatement statement,
            int index,
            Order order) throws SQLException {

        statement.setString(
                index,
                order.getStatus() != null ? order.getStatus().name() : null
        );
    }

    // SET ORDER DATE/TIME

    private void setOrderDateTime(
            PreparedStatement statement,
            int index,
            Order order) throws SQLException {

        statement.setString(
                index,
                order.getOrderDateTime() != null
                        ? order.getOrderDateTime().toString()
                        : null
        );
    }

    // SET PAYMENT STATUS

    private void setPaymentStatus(
            PreparedStatement statement,
            int index,
            Order order) throws SQLException {

        statement.setString(
                index,
                order.getPaymentStatus() != null
                        ? order.getPaymentStatus()
                        : "PENDING"
        );
    }

    // SET COUPON CODE

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
