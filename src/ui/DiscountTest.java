package ui;

import dao.CustomerDAO;
import model.Customer;
import model.Order;
import model.Restaurant;
import model.OrderItem;
import model.MenuItem;
import model.Coupon;
import service.DiscountCalculator;

import java.time.LocalDate;
import java.time.LocalTime;

public class DiscountTest {

    public static void main(String[] args) {

        CustomerDAO customerDAO = new CustomerDAO();
        DiscountCalculator calculator = new DiscountCalculator();

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
        // Test 1: GOLD customer
        // ------------------------------------------------

        Customer goldCustomer = customerDAO.findById(1);

        Order goldOrder = new Order(
                1001,
                goldCustomer,
                restaurant
        );

        goldOrder.addItem(new OrderItem(biryani, 2));

        double goldDiscount =
                calculator.calculateTotalDiscount(goldOrder);

        System.out.println("Test 1 - GOLD Customer");
        System.out.println("Subtotal: Rs.440.0");
        System.out.println("Discount: Rs." + goldDiscount);
        System.out.println();


        // ------------------------------------------------
        // Test 2: PLATINUM customer
        // ------------------------------------------------

        Customer platinumCustomer = customerDAO.findById(3);

        Order platinumOrder = new Order(
                1002,
                platinumCustomer,
                restaurant
        );

        platinumOrder.addItem(new OrderItem(biryani, 2));

        double platinumDiscount =
                calculator.calculateTotalDiscount(platinumOrder);

        System.out.println("Test 2 - PLATINUM Customer");
        System.out.println("Subtotal: Rs.440.0");
        System.out.println("Discount: Rs." + platinumDiscount);
        System.out.println();


        // ------------------------------------------------
        // Test 3: Bulk order
        // ------------------------------------------------

        Order bulkOrder = new Order(
                1003,
                goldCustomer,
                restaurant
        );

        bulkOrder.addItem(new OrderItem(biryani, 5));

        double bulkDiscount =
                calculator.calculateTotalDiscount(bulkOrder);

        System.out.println("Test 3 - Bulk Order");
        System.out.println("Subtotal: Rs.1100.0");
        System.out.println("Discount: Rs." + bulkDiscount);
        System.out.println();


        // ------------------------------------------------
        // Test 4: GOLD + Bulk + Coupon
        // ------------------------------------------------

        Coupon coupon = new Coupon(
                "SAVE10",
                "10% discount",
                10.0,
                200.0,
                500.0,
                LocalDate.now().plusDays(30),
                true
        );

        Order combinedOrder = new Order(
                1004,
                goldCustomer,
                restaurant
        );

        combinedOrder.addItem(new OrderItem(biryani, 5));
        combinedOrder.setAppliedCoupon(coupon);

        double combinedDiscount =
                calculator.calculateTotalDiscount(combinedOrder);

        System.out.println("Test 4 - GOLD + Bulk + Coupon");
        System.out.println("Subtotal: Rs.1100.0");
        System.out.println("Discount: Rs." + combinedDiscount);
        System.out.println();


        // ------------------------------------------------
        // Test 5: Discount cap
        // ------------------------------------------------

        Coupon largeCoupon = new Coupon(
                "SAVE50",
                "50% discount",
                50.0,
                1000.0,
                500.0,
                LocalDate.now().plusDays(30),
                true
        );

        Order cappedOrder = new Order(
                1005,
                platinumCustomer,
                restaurant
        );

        cappedOrder.addItem(new OrderItem(biryani, 5));
        cappedOrder.setAppliedCoupon(largeCoupon);

        double cappedDiscount =
                calculator.calculateTotalDiscount(cappedOrder);

        System.out.println("Test 5 - Maximum Discount Cap");
        System.out.println("Subtotal: Rs.1100.0");
        System.out.println("Maximum Allowed: Rs.330.0");
        System.out.println("Actual Discount: Rs." + cappedDiscount);
    }
}