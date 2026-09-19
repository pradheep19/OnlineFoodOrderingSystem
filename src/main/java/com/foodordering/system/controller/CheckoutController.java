package com.foodordering.system.controller;

import com.foodordering.system.dao.CustomerDAO;
import com.foodordering.system.dao.OrderDAO;
import com.foodordering.system.dao.RestaurantDAO;

import com.foodordering.system.model.Cart;
import com.foodordering.system.model.Coupon;
import com.foodordering.system.model.Customer;
import com.foodordering.system.model.DemandLevel;
import com.foodordering.system.model.LoyaltyTier;
import com.foodordering.system.model.MenuItem;
import com.foodordering.system.model.Order;
import com.foodordering.system.model.OrderItem;
import com.foodordering.system.model.OrderStatus;
import com.foodordering.system.model.Restaurant;

import com.foodordering.system.service.DeliveryFeeCalculator;
import com.foodordering.system.service.DiscountCalculator;
import com.foodordering.system.service.LoyaltyService;
import com.foodordering.system.service.OrderValidator;
import com.foodordering.system.service.PricingEngine;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;


@Controller
public class CheckoutController {


    // =========================================================
    // DAOs AND SERVICES
    // =========================================================

    private final CustomerDAO customerDAO;

    private final OrderDAO orderDAO;

    private final RestaurantDAO restaurantDAO;

    private final PricingEngine pricingEngine;

    private final DiscountCalculator discountCalculator;

    private final OrderValidator orderValidator;

    private final LoyaltyService loyaltyService;

    private final DeliveryFeeCalculator deliveryFeeCalculator;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public CheckoutController(
            CustomerDAO customerDAO,
            OrderDAO orderDAO,
            RestaurantDAO restaurantDAO,
            PricingEngine pricingEngine,
            DiscountCalculator discountCalculator,
            OrderValidator orderValidator,
            LoyaltyService loyaltyService,
            DeliveryFeeCalculator deliveryFeeCalculator) {

        this.customerDAO = customerDAO;
        this.orderDAO = orderDAO;
        this.restaurantDAO = restaurantDAO;
        this.pricingEngine = pricingEngine;
        this.discountCalculator = discountCalculator;
        this.orderValidator = orderValidator;
        this.loyaltyService = loyaltyService;
        this.deliveryFeeCalculator = deliveryFeeCalculator;
    }


    // =========================================================
    // GET LOGGED-IN CUSTOMER
    // =========================================================

    private Customer getLoggedInCustomer(
            HttpSession session) {

        return (Customer) session.getAttribute(
                "loggedInCustomer"
        );
    }


    // =========================================================
    // GET CART
    // =========================================================

    private Cart getCart(
            HttpSession session) {

        Cart cart =
                (Cart) session.getAttribute("cart");


        if (cart != null) {

            return cart;
        }


        Customer customer =
                getLoggedInCustomer(session);


        if (customer == null) {

            return null;
        }


        cart =
                new Cart(
                        1,
                        customer
                );


        session.setAttribute(
                "cart",
                cart
        );


        return cart;
    }


    // =========================================================
    // GET RESTAURANT
    // =========================================================

    private Restaurant getRestaurant(
            Cart cart) {

        if (cart == null ||
                cart.getItems() == null ||
                cart.getItems().isEmpty()) {

            return null;
        }


        int restaurantId =
                cart.getItems()
                        .get(0)
                        .getMenuItem()
                        .getRestaurantId();


        return restaurantDAO.findById(
                restaurantId
        );
    }


    // =========================================================
    // CHECKOUT PAGE
    // =========================================================

    @GetMapping("/checkout")
    public String checkout(
            HttpSession session,
            Model model) {


        Customer customer =
                getLoggedInCustomer(session);


        if (customer == null) {

            return "redirect:/login";
        }


        Cart cart =
                getCart(session);


        if (cart == null) {

            return "redirect:/login";
        }


        Restaurant restaurant =
                getRestaurant(cart);


        // =====================================================
        // EMPTY CART
        // =====================================================

        if (cart.getItems() == null ||
                cart.getItems().isEmpty()) {


            model.addAttribute(
                    "error",
                    "Your cart is empty. Please add items before checkout."
            );


            model.addAttribute(
                    "cart",
                    cart
            );


            model.addAttribute(
                    "customer",
                    customer
            );


            model.addAttribute(
                    "restaurant",
                    restaurant
            );


            return "checkout";
        }


        // =====================================================
        // SUBTOTAL
        // =====================================================

        double subtotal =
                cart.getSubtotal();


        // =====================================================
        // DELIVERY
        // =====================================================

        double deliveryFee =
                0.0;


        if (restaurant != null) {

            deliveryFee =
                    deliveryFeeCalculator.calculateDeliveryFee(
                            customer,
                            restaurant
                    );
        }


        model.addAttribute(
                "cart",
                cart
        );


        model.addAttribute(
                "customer",
                customer
        );


        model.addAttribute(
                "restaurant",
                restaurant
        );


        model.addAttribute(
                "subtotal",
                subtotal
        );


        model.addAttribute(
                "deliveryFee",
                deliveryFee
        );


        return "checkout";
    }


    // =========================================================
    // PLACE ORDER
    // =========================================================

    @PostMapping("/checkout/place-order")
    public String placeOrder(
            @RequestParam(required = false)
            String couponCode,

            HttpSession session,

            Model model) {


        // =====================================================
        // CHECK LOGIN
        // =====================================================

        Customer customer =
                getLoggedInCustomer(session);


        if (customer == null) {

            return "redirect:/login";
        }


        // =====================================================
        // GET CART
        // =====================================================

        Cart cart =
                getCart(session);


        if (cart == null) {

            return "redirect:/login";
        }


        Restaurant restaurant =
                getRestaurant(cart);


        // =====================================================
        // EMPTY CART
        // =====================================================

        if (cart.getItems() == null ||
                cart.getItems().isEmpty()) {


            model.addAttribute(
                    "error",
                    "Your cart is empty. Please add items before checkout."
            );


            model.addAttribute(
                    "cart",
                    cart
            );


            model.addAttribute(
                    "customer",
                    customer
            );


            model.addAttribute(
                    "restaurant",
                    restaurant
            );


            return "checkout";
        }


        // =====================================================
        // RESTAURANT CHECK
        // =====================================================

        if (restaurant == null) {


            model.addAttribute(
                    "error",
                    "Restaurant information could not be found."
            );


            model.addAttribute(
                    "cart",
                    cart
            );


            model.addAttribute(
                    "customer",
                    customer
            );


            return "checkout";
        }


        // =====================================================
        // LOYALTY INFORMATION BEFORE ORDER
        // =====================================================

        LoyaltyTier loyaltyTierBeforeOrder =
                customer.getLoyaltyTier();


        double loyaltyPointsBeforeOrder =
                customer.getLoyaltyPoints();


        double loyaltyDiscountPercentage =
                loyaltyService.getLoyaltyDiscountPercentage(
                        customer
                );


        // =====================================================
        // CREATE ORDER
        // =====================================================

        int orderId =
                orderDAO.getOrderCount() + 1;


        Order order =
                new Order(
                        orderId,
                        customer,
                        restaurant
                );


        // =====================================================
        // COPY CART ITEMS
        // =====================================================

        for (OrderItem cartItem :
                cart.getItems()) {


            order.getItems().add(
                    cartItem
            );
        }


        // =====================================================
        // VALIDATE ORDER
        // =====================================================

        LocalDateTime orderTime =
                LocalDateTime.now();


        String validationError =
        orderValidator.getValidationError(
                order,
                orderTime
        );

if (validationError != null) {

    model.addAttribute(
            "error",
            validationError
    );

    model.addAttribute(
            "cart",
            cart
    );

    model.addAttribute(
            "customer",
            customer
    );

    model.addAttribute(
            "restaurant",
            restaurant
    );

    return "checkout";
}


        // =====================================================
        // DYNAMIC PRICING
        // =====================================================

        DemandLevel demandLevel =
                DemandLevel.HIGH;


        double originalSubtotal =
                0.0;


        double dynamicSubtotal =
                0.0;


        for (OrderItem item :
                order.getItems()) {


            MenuItem menuItem =
                    item.getMenuItem();


            double originalPrice =
                    menuItem.getBasePrice();


            double dynamicPrice =
                    pricingEngine.calculateDynamicPrice(
                            menuItem,
                            orderTime,
                            demandLevel
                    );


            item.setItemPrice(
                    dynamicPrice
            );


            originalSubtotal +=
                    originalPrice *
                            item.getQuantity();


            dynamicSubtotal +=
                    dynamicPrice *
                            item.getQuantity();
        }


        // =====================================================
        // DYNAMIC PRICING AMOUNT
        // =====================================================

        double dynamicPricingAmount =
                dynamicSubtotal -
                        originalSubtotal;


        order.setSubtotal(
                dynamicSubtotal
        );


        order.setDynamicPricingAmount(
                dynamicPricingAmount
        );


        // =====================================================
        // COUPON
        // =====================================================

        Coupon appliedCoupon =
                null;


        if (couponCode != null &&
                !couponCode.trim().isEmpty()) {


            String code =
                    couponCode
                            .trim()
                            .toUpperCase();


            // =================================================
            // SAVE10
            // =================================================

            if ("SAVE10".equals(code)) {


                appliedCoupon =
                        new Coupon(
                                "SAVE10",
                                "10% off up to Rs.100",
                                10.0,
                                100.0,
                                300.0,
                                null,
                                true
                        );
            }


            // =================================================
            // FOOD20
            // =================================================

            else if ("FOOD20".equals(code)) {


                appliedCoupon =
                        new Coupon(
                                "FOOD20",
                                "20% off up to Rs.150",
                                20.0,
                                150.0,
                                500.0,
                                null,
                                true
                        );
            }


            // =================================================
            // INVALID COUPON
            // =================================================

            else {


                model.addAttribute(
                        "error",
                        "Invalid coupon code."
                );


                model.addAttribute(
                        "cart",
                        cart
                );


                model.addAttribute(
                        "customer",
                        customer
                );


                model.addAttribute(
                        "restaurant",
                        restaurant
                );


                model.addAttribute(
                        "subtotal",
                        dynamicSubtotal
                );


                return "checkout";
            }
        }


        // =====================================================
        // SET COUPON
        // =====================================================

        order.setAppliedCoupon(
                appliedCoupon
        );


        // =====================================================
        // CALCULATE INDIVIDUAL DISCOUNTS
        // =====================================================

        /*
         * The discounts are calculated against the
         * dynamically priced subtotal.
         */

        double loyaltyDiscountAmount =
                discountCalculator.calculateLoyaltyDiscount(
                        customer,
                        dynamicSubtotal
                );


        double bulkDiscountAmount =
                discountCalculator.calculateBulkDiscount(
                        order.getTotalItemCount(),
                        dynamicSubtotal
                );


        double couponDiscountAmount =
                discountCalculator.calculateCouponDiscount(
                        appliedCoupon,
                        dynamicSubtotal
                );


        // =====================================================
        // RAW TOTAL DISCOUNT
        // =====================================================

        double rawDiscountAmount =
                loyaltyDiscountAmount
                        + bulkDiscountAmount
                        + couponDiscountAmount;


        // =====================================================
        // 30% MAXIMUM DISCOUNT CAP
        // =====================================================

        double maximumAllowedDiscount =
                dynamicSubtotal * 0.30;


        double discountAmount =
                rawDiscountAmount;


        boolean discountCapped =
                false;


        if (discountAmount >
                maximumAllowedDiscount) {


            discountAmount =
                    maximumAllowedDiscount;


            discountCapped =
                    true;
        }


        // Round final discount
        discountAmount =
                Math.round(
                        discountAmount * 100.0
                ) / 100.0;


        // =====================================================
        // SET DISCOUNT
        // =====================================================

        order.setDiscountAmount(
                discountAmount
        );


        // =====================================================
        // DELIVERY FEE
        // =====================================================

        double deliveryFee =
                deliveryFeeCalculator.calculateDeliveryFee(
                        customer,
                        restaurant
                );


        if (deliveryFee < 0) {


            model.addAttribute(
                    "error",
                    "Delivery is not available for your location."
            );


            model.addAttribute(
                    "cart",
                    cart
            );


            model.addAttribute(
                    "customer",
                    customer
            );


            model.addAttribute(
                    "restaurant",
                    restaurant
            );


            return "checkout";
        }


        order.setDeliveryFee(
                deliveryFee
        );


        // =====================================================
        // FINAL AMOUNT
        // =====================================================

        double finalAmount =
                dynamicSubtotal
                        - discountAmount
                        + deliveryFee;


        if (finalAmount < 0) {

            finalAmount =
                    0.0;
        }


        finalAmount =
                Math.round(
                        finalAmount * 100.0
                ) / 100.0;


        order.setFinalAmount(
                finalAmount
        );


        // =====================================================
        // ORDER STATUS
        // =====================================================

        order.setStatus(
                OrderStatus.CONFIRMED
        );


        order.setOrderDateTime(
                orderTime
        );


        // =====================================================
        // SAVE ORDER
        // =====================================================

        boolean saved =
                orderDAO.saveOrder(
                        order
                );


        if (!saved) {


            model.addAttribute(
                    "error",
                    "Unable to save the order. Please try again."
            );


            model.addAttribute(
                    "cart",
                    cart
            );


            model.addAttribute(
                    "customer",
                    customer
            );


            model.addAttribute(
                    "restaurant",
                    restaurant
            );


            return "checkout";
        }


        // =====================================================
        // LOYALTY POINTS
        // =====================================================

        loyaltyService.processLoyaltyPoints(
                customer,
                finalAmount
        );


        // =====================================================
        // POINTS EARNED
        // =====================================================

        double loyaltyPointsEarned =
                customer.getLoyaltyPoints()
                        - loyaltyPointsBeforeOrder;


        // =====================================================
        // UPDATE CUSTOMER
        // =====================================================

        customerDAO.updateCustomer(
                customer
        );


        // =====================================================
        // SEND ORDER DATA
        // =====================================================

        model.addAttribute(
                "order",
                order
        );


        model.addAttribute(
                "customer",
                customer
        );


        model.addAttribute(
                "restaurant",
                restaurant
        );


        // =====================================================
        // PRICE BREAKDOWN
        // =====================================================

        model.addAttribute(
                "originalSubtotal",
                originalSubtotal
        );


        model.addAttribute(
                "dynamicSubtotal",
                dynamicSubtotal
        );


        model.addAttribute(
                "dynamicPricingAmount",
                dynamicPricingAmount
        );


        model.addAttribute(
                "deliveryFee",
                deliveryFee
        );


        model.addAttribute(
                "finalAmount",
                finalAmount
        );


        // =====================================================
        // DISCOUNT BREAKDOWN
        // =====================================================

        model.addAttribute(
                "loyaltyDiscountAmount",
                loyaltyDiscountAmount
        );


        model.addAttribute(
                "bulkDiscountAmount",
                bulkDiscountAmount
        );


        model.addAttribute(
                "couponDiscountAmount",
                couponDiscountAmount
        );


        model.addAttribute(
                "rawDiscountAmount",
                rawDiscountAmount
        );


        model.addAttribute(
                "maximumAllowedDiscount",
                maximumAllowedDiscount
        );


        model.addAttribute(
                "discountAmount",
                discountAmount
        );


        model.addAttribute(
                "discountCapped",
                discountCapped
        );


        // =====================================================
        // COUPON INFORMATION
        // =====================================================

        if (appliedCoupon != null) {

    model.addAttribute(
            "couponCode",
            couponCode
    );

} else {

    model.addAttribute(
            "couponCode",
            null
    );
}


        // =====================================================
        // LOYALTY INFORMATION
        // =====================================================

        model.addAttribute(
                "loyaltyTierBeforeOrder",
                loyaltyTierBeforeOrder
        );


        model.addAttribute(
                "loyaltyPointsBeforeOrder",
                loyaltyPointsBeforeOrder
        );


        model.addAttribute(
                "loyaltyDiscountPercentage",
                loyaltyDiscountPercentage
        );


        model.addAttribute(
                "loyaltyPointsEarned",
                loyaltyPointsEarned
        );


        // =====================================================
        // CLEAR CART
        // =====================================================

        cart.clear();


        session.setAttribute(
                "cart",
                cart
        );


        // =====================================================
        // SHOW CONFIRMATION
        // =====================================================

        return "order-confirmation";
    }
}