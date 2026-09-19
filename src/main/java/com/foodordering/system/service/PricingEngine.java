package com.foodordering.system.service;

import org.springframework.stereotype.Service;
import com.foodordering.system.model.DemandLevel;
import com.foodordering.system.model.MenuItem;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class PricingEngine {

    // Dynamic pricing configuration
    private static final double PEAK_SURGE = 0.20;
    private static final double WEEKEND_SURCHARGE = 0.10;
    private static final double HIGH_DEMAND_SURGE = 0.15;
    private static final double VERY_HIGH_DEMAND_SURGE = 0.25;

    private static final LocalTime PEAK_START = LocalTime.of(19, 0);
    private static final LocalTime PEAK_END = LocalTime.of(22, 0);

    /**
     * Calculates the dynamically adjusted price of a menu item.
     */
    public double calculateDynamicPrice(MenuItem item,
                                        LocalDateTime orderDateTime,
                                        DemandLevel demandLevel) {

        if (item == null || orderDateTime == null || demandLevel == null) {
            return 0.0;
        }

        double basePrice = item.getBasePrice();

        // Some categories/items can be exempt from dynamic pricing.
        if (item.isDynamicPricingExempt()) {
            return basePrice;
        }

        double adjustedPrice = basePrice;

        // Apply peak-hour surge.
        if (isPeakHour(orderDateTime.toLocalTime())) {
            adjustedPrice += basePrice * PEAK_SURGE;
        }

        // Apply weekend surcharge.
        if (isWeekend(orderDateTime.getDayOfWeek())) {
            adjustedPrice += basePrice * WEEKEND_SURCHARGE;
        }

        // Apply demand-based surge.
        adjustedPrice += basePrice * getDemandSurcharge(demandLevel);

        return roundPrice(adjustedPrice);
    }

    /**
     * Checks whether the given time falls within peak hours.
     */
    private boolean isPeakHour(LocalTime time) {
        return !time.isBefore(PEAK_START)
                && time.isBefore(PEAK_END);
    }

    /**
     * Checks whether the given day is Saturday or Sunday.
     */
    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY
                || day == DayOfWeek.SUNDAY;
    }

    /**
     * Returns the surcharge percentage based on demand.
     */
    private double getDemandSurcharge(DemandLevel demandLevel) {

        switch (demandLevel) {
            case HIGH:
                return HIGH_DEMAND_SURGE;

            case VERY_HIGH:
                return VERY_HIGH_DEMAND_SURGE;

            case LOW:
            case NORMAL:
            default:
                return 0.0;
        }
    }

    /**
     * Rounds the price to two decimal places.
     */
    private double roundPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }
}
