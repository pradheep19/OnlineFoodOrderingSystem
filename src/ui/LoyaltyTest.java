package ui;

import dao.CustomerDAO;
import model.Customer;
import model.LoyaltyTier;
import service.LoyaltyService;

public class LoyaltyTest {

    public static void main(String[] args) {

        CustomerDAO customerDAO = new CustomerDAO();
        LoyaltyService loyaltyService = new LoyaltyService();

        // ---------------------------------------------
        // Test 1: Add loyalty points
        // ---------------------------------------------

        Customer customer = customerDAO.findById(1);

        System.out.println("Test 1 - Add Loyalty Points");

        System.out.println("Initial Points: "
                + customer.getLoyaltyPoints());

        loyaltyService.addPoints(customer, 200);

        System.out.println("Points After Rs.200 Order: "
                + customer.getLoyaltyPoints());

        System.out.println();


        // ---------------------------------------------
        // Test 2: Determine loyalty tier
        // ---------------------------------------------

        System.out.println("Test 2 - Determine Loyalty Tier");

        System.out.println("400 Points: "
                + loyaltyService.determineTier(400));

        System.out.println("700 Points: "
                + loyaltyService.determineTier(700));

        System.out.println("1200 Points: "
                + loyaltyService.determineTier(1200));

        System.out.println("2500 Points: "
                + loyaltyService.determineTier(2500));

        System.out.println();


        // ---------------------------------------------
        // Test 3: Update customer loyalty tier
        // ---------------------------------------------

        System.out.println("Test 3 - Update Customer Tier");

        Customer testCustomer = customerDAO.findById(2);

        System.out.println("Current Points: "
                + testCustomer.getLoyaltyPoints());

        System.out.println("Current Tier: "
                + testCustomer.getLoyaltyTier());

        testCustomer.setLoyaltyPoints(1200);

        loyaltyService.updateLoyaltyTier(testCustomer);

        System.out.println("Updated Points: "
                + testCustomer.getLoyaltyPoints());

        System.out.println("Updated Tier: "
                + testCustomer.getLoyaltyTier());

        System.out.println();


        // ---------------------------------------------
        // Test 4: Process order and update tier
        // ---------------------------------------------

        System.out.println("Test 4 - Process Loyalty Points");

        Customer newCustomer = new Customer(
                10,
                "Test Customer",
                "9999999999",
                "Vellore",
                3.0,
                LoyaltyTier.REGULAR,
                400
        );

        System.out.println("Before Order:");
        System.out.println("Points: "
                + newCustomer.getLoyaltyPoints());
        System.out.println("Tier: "
                + newCustomer.getLoyaltyTier());

        loyaltyService.processLoyaltyPoints(
                newCustomer,
                200
        );

        System.out.println("After Rs.200 Order:");
        System.out.println("Points: "
                + newCustomer.getLoyaltyPoints());
        System.out.println("Tier: "
                + newCustomer.getLoyaltyTier());

        System.out.println();


        // ---------------------------------------------
        // Test 5: Loyalty discount percentage
        // ---------------------------------------------

        System.out.println("Test 5 - Loyalty Discount Percentage");

        Customer regular = new Customer(
                11, "Regular", "9000000001",
                "Vellore", 2.0,
                LoyaltyTier.REGULAR, 100
        );

        Customer silver = new Customer(
                12, "Silver", "9000000002",
                "Vellore", 2.0,
                LoyaltyTier.SILVER, 600
        );

        Customer gold = new Customer(
                13, "Gold", "9000000003",
                "Vellore", 2.0,
                LoyaltyTier.GOLD, 1200
        );

        Customer platinum = new Customer(
                14, "Platinum", "9000000004",
                "Vellore", 2.0,
                LoyaltyTier.PLATINUM, 2500
        );

        System.out.println("Regular: "
                + loyaltyService.getLoyaltyDiscountPercentage(regular)
                + "%");

        System.out.println("Silver: "
                + loyaltyService.getLoyaltyDiscountPercentage(silver)
                + "%");

        System.out.println("Gold: "
                + loyaltyService.getLoyaltyDiscountPercentage(gold)
                + "%");

        System.out.println("Platinum: "
                + loyaltyService.getLoyaltyDiscountPercentage(platinum)
                + "%");
    }
}