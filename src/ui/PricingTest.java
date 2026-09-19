package ui;

import model.DemandLevel;
import model.MenuItem;
import service.PricingEngine;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class PricingTest {

    public static void main(String[] args) {

        PricingEngine pricingEngine = new PricingEngine();

        MenuItem biryani = new MenuItem(
                101,
                "Chicken Biryani",
                "Main Course",
                220.0,
                true,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(22, 30)
        );

        // Test 1: Normal weekday
        LocalDateTime normalTime =
                LocalDateTime.of(2026, 9, 16, 14, 0);

        double price1 = pricingEngine.calculateDynamicPrice(
                biryani,
                normalTime,
                DemandLevel.NORMAL
        );

        System.out.println("Test 1 - Normal Weekday");
        System.out.println("Price: Rs." + price1);
        System.out.println();

        // Test 2: Peak hour
        LocalDateTime peakTime =
                LocalDateTime.of(2026, 9, 16, 20, 0);

        double price2 = pricingEngine.calculateDynamicPrice(
                biryani,
                peakTime,
                DemandLevel.NORMAL
        );

        System.out.println("Test 2 - Peak Hour");
        System.out.println("Price: Rs." + price2);
        System.out.println();

        // Test 3: Weekend + High Demand
        LocalDateTime weekendHighDemand =
                LocalDateTime.of(2026, 9, 19, 20, 0);

        double price3 = pricingEngine.calculateDynamicPrice(
                biryani,
                weekendHighDemand,
                DemandLevel.HIGH
        );

        System.out.println("Test 3 - Weekend + Peak Hour + High Demand");
        System.out.println("Price: Rs." + price3);
        System.out.println();

        // Test 4: Very High Demand
        LocalDateTime veryHighDemand =
                LocalDateTime.of(2026, 9, 19, 20, 0);

        double price4 = pricingEngine.calculateDynamicPrice(
                biryani,
                veryHighDemand,
                DemandLevel.VERY_HIGH
        );

        System.out.println("Test 4 - Weekend + Peak Hour + Very High Demand");
        System.out.println("Price: Rs." + price4);
    }
}