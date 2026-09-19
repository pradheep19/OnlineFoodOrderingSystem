package ui;

import dao.CustomerDAO;
import dao.MenuDAO;
import dao.OrderDAO;

import model.Cart;
import model.Customer;
import model.DemandLevel;
import model.MenuItem;
import model.Order;
import model.OrderItem;
import model.Restaurant;

import service.DeliveryFeeCalculator;
import service.DiscountCalculator;
import service.LoyaltyService;
import service.OrderValidator;
import service.PricingEngine;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static final MenuDAO menuDAO = new MenuDAO();
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final OrderDAO orderDAO = new OrderDAO();

    private static final PricingEngine pricingEngine =
            new PricingEngine();

    private static final DiscountCalculator discountCalculator =
            new DiscountCalculator();

    private static final OrderValidator orderValidator =
            new OrderValidator();

    private static final LoyaltyService loyaltyService =
            new LoyaltyService();

    private static final DeliveryFeeCalculator deliveryFeeCalculator =
            new DeliveryFeeCalculator();

    public static void main(String[] args) {

        Restaurant restaurant = createRestaurant();

        System.out.println("==========================================");
        System.out.println("     ONLINE FOOD ORDERING SYSTEM");
        System.out.println("          Dynamic Pricing");
        System.out.println("==========================================");

        Customer customer = selectCustomer();

        if (customer == null) {
            System.out.println("Invalid customer.");
            return;
        }

        Cart cart = new Cart(1, customer);

        boolean shopping = true;

        while (shopping) {

            displayMenu();

            System.out.println();
            System.out.println("1. Add Item");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {

                case 1:
                    addItemToCart(cart);
                    break;

                case 2:
                    displayCart(cart);
                    break;

                case 3:
                    if (checkout(cart, customer, restaurant)) {
                        shopping = false;
                    }
                    break;

                case 4:
                    System.out.println("Thank you for using the system.");
                    shopping = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }

    private static Restaurant createRestaurant() {

        return new Restaurant(
                1,
                "FoodHub",
                "Vellore",
                LocalTime.of(10, 0),
                LocalTime.of(23, 0),
                10.0,
                100.0
        );
    }

    private static Customer selectCustomer() {

        System.out.println();
        System.out.println("Available Customers");
        System.out.println("--------------------");

        for (Customer customer : customerDAO.findAll()) {
            System.out.println(
                    customer.getCustomerId()
                    + ". "
                    + customer.getName()
                    + " - "
                    + customer.getLoyaltyTier()
            );
        }

        System.out.print("Enter Customer ID: ");

        int customerId = readInt();

        return customerDAO.findById(customerId);
    }

    private static void displayMenu() {

        System.out.println();
        System.out.println("============== MENU ==============");

        for (MenuItem item : menuDAO.findAvailableItems()) {

            System.out.println(
                    item.getItemId()
                    + " | "
                    + item.getName()
                    + " | "
                    + item.getCategory()
                    + " | Rs."
                    + item.getBasePrice()
            );
        }

        System.out.println("==================================");
    }

    private static void addItemToCart(Cart cart) {

        System.out.print("Enter Item ID: ");
        int itemId = readInt();

        MenuItem item = menuDAO.findById(itemId);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        if (!item.isAvailable()) {
            System.out.println("Item is currently unavailable.");
            return;
        }

        System.out.print("Enter quantity: ");
        int quantity = readInt();

        if (quantity <= 0) {
            System.out.println("Quantity must be greater than zero.");
            return;
        }

        OrderItem orderItem =
                new OrderItem(item, quantity);

        cart.addItem(orderItem);

        System.out.println(
                item.getName()
                + " added to cart."
        );
    }

    private static void displayCart(Cart cart) {

        System.out.println();
        System.out.println("============== CART ==============");

        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        for (OrderItem item : cart.getItems()) {

            System.out.println(
                    item.getMenuItem().getName()
                    + " x "
                    + item.getQuantity()
                    + " = Rs."
                    + item.getTotalPrice()
            );
        }

        System.out.println("----------------------------------");
        System.out.println(
                "Subtotal: Rs."
                + cart.getSubtotal()
        );

        System.out.println(
                "Total Items: "
                + cart.getTotalItemCount()
        );

        System.out.println("==================================");
    }

    private static boolean checkout(Cart cart,
                                    Customer customer,
                                    Restaurant restaurant) {

        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return false;
        }

        LocalDateTime orderDateTime =
                LocalDateTime.now();

        Order order = new Order(
                orderDAO.getOrderCount() + 1,
                customer,
                restaurant
        );

        for (OrderItem item : cart.getItems()) {
            order.addItem(
                    new OrderItem(
                            item.getMenuItem(),
                            item.getQuantity()
                    )
            );
        }

        System.out.println();
        System.out.println("========== CHECKOUT ==========");

        // Validate order
        if (!orderValidator.validateOrder(
                order,
                orderDateTime)) {

            System.out.println(
                    "Order validation failed."
            );

            System.out.println(
                    "Please check restaurant hours, "
                    + "item availability, delivery radius, "
                    + "and minimum order value."
            );

            return false;
        }

        // Ask for demand level
        DemandLevel demandLevel =
                selectDemandLevel();

        // Apply dynamic pricing
        double dynamicTotal = 0.0;

        for (OrderItem item : order.getItems()) {

            double dynamicPrice =
                    pricingEngine.calculateDynamicPrice(
                            item.getMenuItem(),
                            orderDateTime,
                            demandLevel
                    );

            item.setItemPrice(dynamicPrice);

            dynamicTotal +=
                    dynamicPrice * item.getQuantity();
        }

        double originalSubtotal =
                order.calculateItemSubtotal();

        double dynamicPricingAmount =
                dynamicTotal - originalSubtotal;

        order.setSubtotal(originalSubtotal);
        order.setDynamicPricingAmount(
                dynamicPricingAmount
        );

        // Calculate discounts
        double discount =
                discountCalculator.calculateTotalDiscount(
                        order
                );

        order.setDiscountAmount(discount);

        // Calculate delivery fee
        double deliveryFee =
                deliveryFeeCalculator.calculateDeliveryFee(
                        customer,
                        restaurant
                );

        if (deliveryFee < 0) {
            System.out.println(
                    "Delivery is not available "
                    + "for this customer."
            );

            return false;
        }

        order.setDeliveryFee(deliveryFee);

        double finalAmount =
                dynamicTotal
                - discount
                + deliveryFee;

        order.setFinalAmount(finalAmount);

        // Save order
        orderDAO.saveOrder(order);

        // Award loyalty points
        loyaltyService.processLoyaltyPoints(
                customer,
                finalAmount
        );

        // Display bill
        displayFinalBill(
                order,
                dynamicTotal,
                discount,
                deliveryFee,
                finalAmount
        );

        cart.clear();

        return true;
    }

    private static DemandLevel selectDemandLevel() {

        System.out.println();
        System.out.println("Select Current Demand Level");
        System.out.println("1. LOW");
        System.out.println("2. NORMAL");
        System.out.println("3. HIGH");
        System.out.println("4. VERY HIGH");

        System.out.print("Enter choice: ");

        int choice = readInt();

        switch (choice) {

            case 1:
                return DemandLevel.LOW;

            case 2:
                return DemandLevel.NORMAL;

            case 3:
                return DemandLevel.HIGH;

            case 4:
                return DemandLevel.VERY_HIGH;

            default:
                System.out.println(
                        "Invalid choice. Using NORMAL."
                );

                return DemandLevel.NORMAL;
        }
    }

    private static void displayFinalBill(
            Order order,
            double dynamicTotal,
            double discount,
            double deliveryFee,
            double finalAmount) {

        System.out.println();
        System.out.println("==========================================");
        System.out.println("              FINAL BILL");
        System.out.println("==========================================");

        System.out.println(
                "Customer: "
                + order.getCustomer().getName()
        );

        System.out.println(
                "Order ID: "
                + order.getOrderId()
        );

        System.out.println("------------------------------------------");

        for (OrderItem item : order.getItems()) {

            System.out.println(
                    item.getMenuItem().getName()
                    + " x "
                    + item.getQuantity()
                    + " = Rs."
                    + item.getTotalPrice()
            );
        }

        System.out.println("------------------------------------------");

        System.out.println(
                "Original Subtotal: Rs."
                + order.getSubtotal()
        );

        System.out.println(
                "Dynamic Pricing Adjustment: Rs."
                + order.getDynamicPricingAmount()
        );

        System.out.println(
                "Price After Dynamic Pricing: Rs."
                + dynamicTotal
        );

        System.out.println(
                "Discount: -Rs."
                + discount
        );

        System.out.println(
                "Delivery Fee: Rs."
                + deliveryFee
        );

        System.out.println("------------------------------------------");

        System.out.println(
                "FINAL AMOUNT: Rs."
                + finalAmount
        );

        System.out.println("------------------------------------------");

        System.out.println(
                "Loyalty Points: "
                + order.getCustomer().getLoyaltyPoints()
        );

        System.out.println(
                "Loyalty Tier: "
                + order.getCustomer().getLoyaltyTier()
        );

        System.out.println("==========================================");
        System.out.println("       ORDER PLACED SUCCESSFULLY");
        System.out.println("==========================================");
    }

    private static int readInt() {

        while (!scanner.hasNextInt()) {

            System.out.print(
                    "Please enter a valid number: "
            );

            scanner.next();
        }

        return scanner.nextInt();
    }
}
