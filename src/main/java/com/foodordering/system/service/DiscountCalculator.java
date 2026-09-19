package com.foodordering.system.service;
import org.springframework.stereotype.Service;
import com.foodordering.system.model.Coupon;
import com.foodordering.system.model.Customer;
import com.foodordering.system.model.LoyaltyTier;
import com.foodordering.system.model.Order;
import java.time.LocalDate;

/**
 * Calculates discounts applicable to an order.
 *
 * Discount sources:
 * 1. Loyalty discount
 * 2. Bulk-order discount
 * 3. Coupon discount
 *
 * The discounts are calculated independently and then combined,
 * subject to the maximum discount cap.
 */
@Service
public class DiscountCalculator {

    // Loyalty discount rates
    private static final double SILVER_DISCOUNT = 0.05;
    private static final double GOLD_DISCOUNT = 0.10;
    private static final double PLATINUM_DISCOUNT = 0.15;

    // Bulk-order discount configuration
    private static final int BULK_ITEM_THRESHOLD = 5;
    private static final double BULK_DISCOUNT = 0.05;

    // Maximum total discount allowed
    private static final double MAX_DISCOUNT_PERCENTAGE = 0.30;

    /**
     * Calculates the total discount applicable to an order.
     */
    public double calculateTotalDiscount(Order order) {

        if (order == null || order.getItems().isEmpty()) {
            return 0.0;
        }

        double subtotal = order.calculateItemSubtotal();

        double loyaltyDiscount = calculateLoyaltyDiscount(
                order.getCustomer(),
                subtotal
        );

        double bulkDiscount = calculateBulkDiscount(
                order.getTotalItemCount(),
                subtotal
        );

        double couponDiscount = calculateCouponDiscount(
                order.getAppliedCoupon(),
                subtotal
        );

        double totalDiscount =
                loyaltyDiscount + bulkDiscount + couponDiscount;

        // Apply maximum discount cap
        double maximumAllowedDiscount =
                subtotal * MAX_DISCOUNT_PERCENTAGE;

        if (totalDiscount > maximumAllowedDiscount) {
            totalDiscount = maximumAllowedDiscount;
        }

        return roundPrice(totalDiscount);
    }

    /**
     * Calculates discount based on customer's loyalty tier.
     */
    public double calculateLoyaltyDiscount(Customer customer,
                                           double orderValue) {

        if (customer == null || orderValue <= 0) {
            return 0.0;
        }

        LoyaltyTier tier = customer.getLoyaltyTier();

        switch (tier) {
            case SILVER:
                return orderValue * SILVER_DISCOUNT;

            case GOLD:
                return orderValue * GOLD_DISCOUNT;

            case PLATINUM:
                return orderValue * PLATINUM_DISCOUNT;

            case REGULAR:
            default:
                return 0.0;
        }
    }

    /**
     * Calculates bulk-order discount.
     */
    public double calculateBulkDiscount(int totalItemCount,
                                        double orderValue) {

        if (totalItemCount >= BULK_ITEM_THRESHOLD
                && orderValue > 0) {

            return orderValue * BULK_DISCOUNT;
        }

        return 0.0;
    }

    /**
     * Calculates coupon discount if the coupon is valid.
     */
    public double calculateCouponDiscount(Coupon coupon,
                                          double orderValue) {

        if (coupon == null || orderValue <= 0) {
            return 0.0;
        }

        LocalDate currentDate = LocalDate.now();

        if (!coupon.isValid(orderValue, currentDate)) {
            return 0.0;
        }

        return coupon.calculateDiscount(orderValue);
    }

    /**
     * Rounds monetary values to two decimal places.
     */
    private double roundPrice(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}