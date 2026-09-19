package ui;

import model.Customer;
import model.Restaurant;
import model.LoyaltyTier;
import service.DeliveryFeeCalculator;

import java.time.LocalTime;

public class DeliveryTest {

    public static void main(String[] args) {

        DeliveryFeeCalculator calculator =
                new DeliveryFeeCalculator();

        Restaurant restaurant = new Restaurant(
                1,
                "FoodHub",
                "Vellore",
                LocalTime.of(10, 0),
                LocalTime.of(23, 0),
                10.0,
                100.0
        );

        // ---------------------------------------------
        // Test 1: 2 km
        // ---------------------------------------------

        Customer customer1 = new Customer(
                1,
                "Customer 1",
                "9000000001",
                "Vellore",
                2.0,
                LoyaltyTier.REGULAR,
                100
        );

        double fee1 = calculator.calculateDeliveryFee(
                customer1, restaurant
        );

        System.out.println("Test 1 - Distance: 2 km");
        System.out.println("Delivery Fee: Rs." + fee1);
        System.out.println();


        // ---------------------------------------------
        // Test 2: 5 km
        // ---------------------------------------------

        Customer customer2 = new Customer(
                2,
                "Customer 2",
                "9000000002",
                "Vellore",
                5.0,
                LoyaltyTier.SILVER,
                600
        );

        double fee2 = calculator.calculateDeliveryFee(
                customer2, restaurant
        );

        System.out.println("Test 2 - Distance: 5 km");
        System.out.println("Delivery Fee: Rs." + fee2);
        System.out.println();


        // ---------------------------------------------
        // Test 3: 8 km
        // ---------------------------------------------

        Customer customer3 = new Customer(
                3,
                "Customer 3",
                "9000000003",
                "Vellore",
                8.0,
                LoyaltyTier.GOLD,
                1200
        );

        double fee3 = calculator.calculateDeliveryFee(
                customer3, restaurant
        );

        System.out.println("Test 3 - Distance: 8 km");
        System.out.println("Delivery Fee: Rs." + fee3);
        System.out.println();


        // ---------------------------------------------
        // Test 4: 12 km
        // ---------------------------------------------

        Customer customer4 = new Customer(
                4,
                "Customer 4",
                "9000000004",
                "Chennai",
                12.0,
                LoyaltyTier.REGULAR,
                100
        );

        double fee4 = calculator.calculateDeliveryFee(
                customer4, restaurant
        );

        System.out.println("Test 4 - Distance: 12 km");
        System.out.println("Delivery Fee: " + fee4);
        System.out.println();


        // ---------------------------------------------
        // Test 5: Delivery availability
        // ---------------------------------------------

        boolean available1 =
                calculator.isDeliveryAvailable(
                        customer1, restaurant
                );

        boolean available4 =
                calculator.isDeliveryAvailable(
                        customer4, restaurant
                );

        System.out.println("Test 5 - Delivery Availability");
        System.out.println("2 km Customer: " + available1);
        System.out.println("12 km Customer: " + available4);
    }
}