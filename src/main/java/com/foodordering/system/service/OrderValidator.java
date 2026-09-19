package com.foodordering.system.service;
import org.springframework.stereotype.Service;
import com.foodordering.system.model.Customer;
import com.foodordering.system.model.MenuItem;
import com.foodordering.system.model.Order;
import com.foodordering.system.model.Restaurant;

import java.time.LocalDateTime;
import java.time.LocalTime;
@Service
public class OrderValidator {

    /**
     * Validates whether an order can be placed.
     *
     * This method keeps the original boolean-based validation
     * so existing code and tests continue to work.
     */
    public boolean validateOrder(
            Order order,
            LocalDateTime orderDateTime) {

        return getValidationError(
                order,
                orderDateTime
        ) == null;
    }


    /**
     * Returns the exact reason why an order cannot be placed.
     *
     * Returns null when the order is valid.
     */
    public String getValidationError(
            Order order,
            LocalDateTime orderDateTime) {

        // -----------------------------------------------------
        // BASIC ORDER CHECK
        // -----------------------------------------------------

        if (order == null) {
            return "Order information is missing.";
        }

        if (orderDateTime == null) {
            return "Order time is missing.";
        }

        if (order.getItems() == null ||
                order.getItems().isEmpty()) {

            return "Your cart is empty. Please add items before placing the order.";
        }


        // -----------------------------------------------------
        // RESTAURANT CHECK
        // -----------------------------------------------------

        Restaurant restaurant = order.getRestaurant();

        if (restaurant == null) {
            return "Restaurant information could not be found.";
        }

        LocalTime orderTime =
                orderDateTime.toLocalTime();

        if (!isRestaurantOpen(
                restaurant,
                orderTime)) {

            return "Restaurant is currently closed. " +
                    "Operating hours are " +
                    restaurant.getOpeningTime() +
                    " to " +
                    restaurant.getClosingTime() +
                    ".";
        }


        // -----------------------------------------------------
        // ITEM AVAILABILITY CHECK
        // -----------------------------------------------------

        if (order.getItems() != null) {

            for (var orderItem : order.getItems()) {

                if (orderItem == null ||
                        orderItem.getMenuItem() == null) {

                    return "One of the items in your order is invalid.";
                }

                MenuItem item =
                        orderItem.getMenuItem();

                // General availability
                if (!item.isAvailable()) {

                    return item.getName() +
                            " is currently unavailable.";
                }


                // Time-based availability
                if (!isWithinItemAvailability(
                        item,
                        orderTime)) {

                    return item.getName() +
        " is not available at " +
        orderTime.withSecond(0).withNano(0) +
        ". Available from " +
        item.getAvailableFrom() +
        " to " +
        item.getAvailableTo() +
        ".";
                }
            }
        }


        // -----------------------------------------------------
        // DELIVERY RADIUS CHECK
        // -----------------------------------------------------

        Customer customer =
                order.getCustomer();

        if (customer == null) {

            return "Customer information could not be found.";
        }

        if (!isWithinDeliveryRadius(
                customer,
                restaurant)) {

            return "Delivery is not available for your location. " +
                    "Your distance is " +
                    customer.getDistanceFromRestaurant() +
                    " km, while the restaurant's delivery radius is " +
                    restaurant.getDeliveryRadius() +
                    " km.";
        }


        // -----------------------------------------------------
        // MINIMUM ORDER VALUE CHECK
        // -----------------------------------------------------

        if (!meetsMinimumOrderValue(order)) {

            double subtotal =
                    order.calculateItemSubtotal();

            double minimum =
                    restaurant.getMinimumOrderValue();

            return String.format(
                    "Minimum order value is Rs.%.2f. " +
                    "Your current subtotal is Rs.%.2f.",
                    minimum,
                    subtotal
            );
        }


        // -----------------------------------------------------
        // ALL VALID
        // -----------------------------------------------------

        return null;
    }


    /**
     * Checks whether the restaurant is operating
     * at the given time.
     */
    public boolean isRestaurantOpen(
            Restaurant restaurant,
            LocalTime orderTime) {

        if (restaurant == null ||
                orderTime == null) {

            return false;
        }

        LocalTime openingTime =
                restaurant.getOpeningTime();

        LocalTime closingTime =
                restaurant.getClosingTime();

        return !orderTime.isBefore(openingTime)
                && !orderTime.isAfter(closingTime);
    }


    /**
     * Checks availability of every item in the order.
     */
    public boolean areItemsAvailable(
            Order order,
            LocalTime orderTime) {

        if (order == null ||
                orderTime == null) {

            return false;
        }

        if (order.getItems() == null ||
                order.getItems().isEmpty()) {

            return false;
        }

        for (var orderItem : order.getItems()) {

            if (orderItem == null ||
                    orderItem.getMenuItem() == null) {

                return false;
            }

            MenuItem item =
                    orderItem.getMenuItem();

            if (!item.isAvailable()) {
                return false;
            }

            if (!isWithinItemAvailability(
                    item,
                    orderTime)) {

                return false;
            }
        }

        return true;
    }


    /**
     * Checks whether a menu item is available
     * at the requested time.
     */
    public boolean isWithinItemAvailability(
            MenuItem item,
            LocalTime orderTime) {

        if (item == null ||
                orderTime == null) {

            return false;
        }

        LocalTime availableFrom =
                item.getAvailableFrom();

        LocalTime availableTo =
                item.getAvailableTo();

        return !orderTime.isBefore(availableFrom)
                && !orderTime.isAfter(availableTo);
    }


    /**
     * Checks whether the customer is within
     * the restaurant's delivery radius.
     */
    public boolean isWithinDeliveryRadius(
            Customer customer,
            Restaurant restaurant) {

        if (customer == null ||
                restaurant == null) {

            return false;
        }

        return customer.getDistanceFromRestaurant()
                <= restaurant.getDeliveryRadius();
    }


    /**
     * Checks whether the order meets
     * the minimum order value.
     */
    public boolean meetsMinimumOrderValue(
            Order order) {

        if (order == null ||
                order.getRestaurant() == null) {

            return false;
        }

        double subtotal =
                order.calculateItemSubtotal();

        return subtotal >=
                order.getRestaurant()
                        .getMinimumOrderValue();
    }
}