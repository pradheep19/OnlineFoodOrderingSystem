package ui;

import dao.CustomerDAO;
import model.Customer;
import model.MenuItem;
import model.Order;
import model.OrderItem;
import model.Restaurant;
import service.OrderValidator;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ValidatorTest {

    public static void main(String[] args) {

        CustomerDAO customerDAO = new CustomerDAO();
        OrderValidator validator = new OrderValidator();

        Restaurant restaurant = new Restaurant(
                1,
                "FoodHub",
                "Vellore",
                LocalTime.of(10, 0),
                LocalTime.of(23, 0),
                10.0,
                100.0
        );

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

        // ------------------------------------------------
        // Test 1: Valid Order
        // ------------------------------------------------

        Customer customer = customerDAO.findById(1);

        Order validOrder = new Order(
                2001,
                customer,
                restaurant
        );

        validOrder.addItem(new OrderItem(biryani, 1));

        boolean result1 = validator.validateOrder(
                validOrder,
                LocalDateTime.of(2026, 9, 16, 14, 0)
        );

        System.out.println("Test 1 - Valid Order");
        System.out.println("Result: " + result1);
        System.out.println();


        // ------------------------------------------------
        // Test 2: Restaurant Closed
        // ------------------------------------------------

        Order closedOrder = new Order(
                2002,
                customer,
                restaurant
        );

        closedOrder.addItem(new OrderItem(biryani, 1));

        boolean result2 = validator.validateOrder(
                closedOrder,
                LocalDateTime.of(2026, 9, 16, 23, 30)
        );

        System.out.println("Test 2 - Restaurant Closed");
        System.out.println("Result: " + result2);
        System.out.println();


        // ------------------------------------------------
        // Test 3: Item Not Available
        // ------------------------------------------------

        MenuItem unavailableItem = new MenuItem(
                106,
                "Special Dish",
                "Main Course",
                250.0,
                false,
                false,
                LocalTime.of(11, 0),
                LocalTime.of(22, 0)
        );

        Order unavailableOrder = new Order(
                2003,
                customer,
                restaurant
        );

        unavailableOrder.addItem(
                new OrderItem(unavailableItem, 1)
        );

        boolean result3 = validator.validateOrder(
                unavailableOrder,
                LocalDateTime.of(2026, 9, 16, 14, 0)
        );

        System.out.println("Test 3 - Item Not Available");
        System.out.println("Result: " + result3);
        System.out.println();


        // ------------------------------------------------
        // Test 4: Item Outside Availability Time
        // ------------------------------------------------

        MenuItem lunchItem = new MenuItem(
                107,
                "Lunch Special",
                "Main Course",
                200.0,
                true,
                false,
                LocalTime.of(12, 0),
                LocalTime.of(15, 0)
        );

        Order timeOrder = new Order(
                2004,
                customer,
                restaurant
        );

        timeOrder.addItem(
                new OrderItem(lunchItem, 1)
        );

        boolean result4 = validator.validateOrder(
                timeOrder,
                LocalDateTime.of(2026, 9, 16, 18, 0)
        );

        System.out.println("Test 4 - Item Outside Availability Time");
        System.out.println("Result: " + result4);
        System.out.println();


        // ------------------------------------------------
        // Test 5: Outside Delivery Radius
        // ------------------------------------------------

        Customer farCustomer = new Customer(
                10,
                "Test Customer",
                "9999999999",
                "Chennai",
                15.0,
                model.LoyaltyTier.REGULAR,
                0
        );

        Order farOrder = new Order(
                2005,
                farCustomer,
                restaurant
        );

        farOrder.addItem(new OrderItem(biryani, 1));

        boolean result5 = validator.validateOrder(
                farOrder,
                LocalDateTime.of(2026, 9, 16, 14, 0)
        );

        System.out.println("Test 5 - Outside Delivery Radius");
        System.out.println("Result: " + result5);
        System.out.println();


        // ------------------------------------------------
        // Test 6: Minimum Order Value Not Met
        // ------------------------------------------------

        Restaurant expensiveMinimumRestaurant = new Restaurant(
                2,
                "Premium FoodHub",
                "Vellore",
                LocalTime.of(10, 0),
                LocalTime.of(23, 0),
                10.0,
                500.0
        );

        Order minimumOrder = new Order(
                2006,
                customer,
                expensiveMinimumRestaurant
        );

        minimumOrder.addItem(new OrderItem(biryani, 1));

        boolean result6 = validator.validateOrder(
                minimumOrder,
                LocalDateTime.of(2026, 9, 16, 14, 0)
        );

        System.out.println("Test 6 - Minimum Order Value Not Met");
        System.out.println("Result: " + result6);
    }
}