package com.foodordering.system.service;
import org.springframework.stereotype.Service;

import com.foodordering.system.model.Customer;
import com.foodordering.system.model.Restaurant;
@Service
public class DeliveryFeeCalculator {

    private static final double FIRST_DISTANCE_LIMIT = 3.0;
    private static final double SECOND_DISTANCE_LIMIT = 6.0;
    private static final double MAX_DELIVERY_DISTANCE = 10.0;

    private static final double FIRST_ZONE_FEE = 30.0;
    private static final double SECOND_ZONE_FEE = 50.0;
    private static final double THIRD_ZONE_FEE = 80.0;

    /**
     * Calculates delivery fee based on customer distance.
     */
    public double calculateDeliveryFee(Customer customer,
                                       Restaurant restaurant) {

        if (customer == null || restaurant == null) {
            return 0.0;
        }

        double distance = customer.getDistanceFromRestaurant();

        if (distance > MAX_DELIVERY_DISTANCE) {
            return -1.0;
        }

        if (distance <= FIRST_DISTANCE_LIMIT) {
            return FIRST_ZONE_FEE;
        }

        if (distance <= SECOND_DISTANCE_LIMIT) {
            return SECOND_ZONE_FEE;
        }

        return THIRD_ZONE_FEE;
    }

    /**
     * Checks whether delivery is possible.
     */
    public boolean isDeliveryAvailable(Customer customer,
                                       Restaurant restaurant) {

        if (customer == null || restaurant == null) {
            return false;
        }

        return customer.getDistanceFromRestaurant()
                <= restaurant.getDeliveryRadius();
    }
}
