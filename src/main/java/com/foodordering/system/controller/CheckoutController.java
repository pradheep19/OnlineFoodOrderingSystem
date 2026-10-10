
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CheckoutController {

    private static final String PENDING_ORDER = "pendingPaymentOrder";
    private static final String ORDER_BREAKDOWN = "pendingOrderBreakdown";

    private static final String CONFIRMED_ORDER = "confirmedOrder";
    private static final String CONFIRMED_BREAKDOWN = "confirmedOrderBreakdown";
    private static final String CONFIRMED_POINTS_EARNED =
            "confirmedLoyaltyPointsEarned";

    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;
    private final RestaurantDAO restaurantDAO;
    private final PricingEngine pricingEngine;
    private final DiscountCalculator discountCalculator;
    private final OrderValidator orderValidator;
    private final LoyaltyService loyaltyService;
    private final DeliveryFeeCalculator deliveryFeeCalculator;

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
    // SESSION HELPERS
    // =========================================================

    private Customer getLoggedInCustomer(HttpSession session) {
        return (Customer) session.getAttribute("loggedInCustomer");
    }

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");

        if (cart != null) {
            return cart;
        }

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return null;
        }

        cart = new Cart(1, customer);
        session.setAttribute("cart", cart);

        return cart;
    }

    private Restaurant getRestaurant(Cart cart) {
        if (cart == null
                || cart.getItems() == null
                || cart.getItems().isEmpty()) {
            return null;
        }

        int restaurantId = cart.getItems()
                .get(0)
                .getMenuItem()
                .getRestaurantId();

        return restaurantDAO.findById(restaurantId);
    }

    private String showCheckoutError(
            Model model,
            Customer customer,
            Cart cart,
            Restaurant restaurant,
            String error) {

        model.addAttribute("error", error);
        model.addAttribute("customer", customer);
        model.addAttribute("cart", cart);
        model.addAttribute("restaurant", restaurant);

        if (cart != null && cart.getItems() != null) {
            model.addAttribute("subtotal", cart.getSubtotal());
        }

        return "checkout";
    }

    // =========================================================
    // ORDER HISTORY
    // =========================================================

    @GetMapping("/orders")
    public String orderHistory(HttpSession session, Model model) {

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return "redirect:/login";
        }

        List<Order> customerOrders = orderDAO.findAll()
                .stream()
                .filter(order ->
                        order.getCustomer() != null
                                && order.getCustomer().getCustomerId()
                                == customer.getCustomerId())
                .sorted((first, second) -> {
                    if (first.getOrderDateTime() == null) {
                        return 1;
                    }

                    if (second.getOrderDateTime() == null) {
                        return -1;
                    }

                    return second.getOrderDateTime()
                            .compareTo(first.getOrderDateTime());
                })
                .toList();

        model.addAttribute("orders", customerOrders);
        model.addAttribute("customer", customer);

        return "order-history";
    }

    // =========================================================
    // CHECKOUT PAGE
    // =========================================================

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return "redirect:/login";
        }

        Cart cart = getCart(session);

        if (cart == null) {
            return "redirect:/login";
        }

        Restaurant restaurant = getRestaurant(cart);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return showCheckoutError(
                    model,
                    customer,
                    cart,
                    restaurant,
                    "Your cart is empty. Please add items before checkout."
            );
        }

        double deliveryFee = restaurant == null
                ? 0.0
                : deliveryFeeCalculator.calculateDeliveryFee(
                        customer, restaurant
                );

        model.addAttribute("customer", customer);
        model.addAttribute("cart", cart);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("subtotal", cart.getSubtotal());
        model.addAttribute("deliveryFee", deliveryFee);

        return "checkout";
    }

    // =========================================================
    // PREPARE ORDER AND CALCULATE TOTAL
    // The order is NOT saved here.
    // =========================================================

    @PostMapping("/checkout/place-order")
    public String placeOrder(
            @RequestParam(required = false) String couponCode,
            HttpSession session,
            Model model) {

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return "redirect:/login";
        }

        Cart cart = getCart(session);

        if (cart == null) {
            return "redirect:/login";
        }

        Restaurant restaurant = getRestaurant(cart);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return showCheckoutError(
                    model,
                    customer,
                    cart,
                    restaurant,
                    "Your cart is empty. Please add items before checkout."
            );
        }

        if (restaurant == null) {
            return showCheckoutError(
                    model,
                    customer,
                    cart,
                    null,
                    "Restaurant information could not be found."
            );
        }

        // Clear previous pending and confirmation data before a new order.
        session.removeAttribute(PENDING_ORDER);
        session.removeAttribute(ORDER_BREAKDOWN);
        session.removeAttribute(CONFIRMED_ORDER);
        session.removeAttribute(CONFIRMED_BREAKDOWN);
        session.removeAttribute(CONFIRMED_POINTS_EARNED);

        LoyaltyTier loyaltyTierBeforeOrder = customer.getLoyaltyTier();
        double loyaltyPointsBeforeOrder = customer.getLoyaltyPoints();

        double loyaltyDiscountPercentage =
                loyaltyService.getLoyaltyDiscountPercentage(customer);

        int orderId = orderDAO.getOrderCount() + 1;

        Order order = new Order(orderId, customer, restaurant);

        for (OrderItem cartItem : cart.getItems()) {
            order.getItems().add(cartItem);
        }

        LocalDateTime orderTime = LocalDateTime.now();

        String validationError =
                orderValidator.getValidationError(order, orderTime);

        if (validationError != null) {
            return showCheckoutError(
                    model, customer, cart, restaurant, validationError
            );
        }

        // -----------------------------------------------------
        // DYNAMIC PRICING
        // -----------------------------------------------------

        DemandLevel demandLevel = DemandLevel.HIGH;

        double originalSubtotal = 0.0;
        double dynamicSubtotal = 0.0;

        for (OrderItem item : order.getItems()) {

            MenuItem menuItem = item.getMenuItem();

            double originalPrice = menuItem.getBasePrice();

            double dynamicPrice = pricingEngine.calculateDynamicPrice(
                    menuItem, orderTime, demandLevel
            );

            item.setItemPrice(dynamicPrice);

            originalSubtotal += originalPrice * item.getQuantity();
            dynamicSubtotal += dynamicPrice * item.getQuantity();
        }

        double dynamicPricingAmount =
                dynamicSubtotal - originalSubtotal;

        order.setSubtotal(dynamicSubtotal);
        order.setDynamicPricingAmount(dynamicPricingAmount);

        // -----------------------------------------------------
        // COUPON VALIDATION
        // -----------------------------------------------------

        Coupon appliedCoupon = null;

        if (couponCode != null && !couponCode.trim().isEmpty()) {

            String code = couponCode.trim().toUpperCase();

            if ("SAVE10".equals(code)) {

                appliedCoupon = new Coupon(
                        "SAVE10",
                        "10% off up to Rs.100",
                        10.0,
                        100.0,
                        300.0,
                        null,
                        true
                );

            } else if ("FOOD20".equals(code)) {

                appliedCoupon = new Coupon(
                        "FOOD20",
                        "20% off up to Rs.150",
                        20.0,
                        150.0,
                        500.0,
                        null,
                        true
                );

            } else {

                return showCheckoutError(
                        model,
                        customer,
                        cart,
                        restaurant,
                        "Invalid coupon code."
                );
            }
        }

        order.setAppliedCoupon(appliedCoupon);

        // -----------------------------------------------------
        // DISCOUNTS
        // -----------------------------------------------------

        double loyaltyDiscountAmount =
                discountCalculator.calculateLoyaltyDiscount(
                        customer, dynamicSubtotal
                );

        double bulkDiscountAmount =
                discountCalculator.calculateBulkDiscount(
                        order.getTotalItemCount(), dynamicSubtotal
                );

        double couponDiscountAmount =
                discountCalculator.calculateCouponDiscount(
                        appliedCoupon, dynamicSubtotal
                );

        double rawDiscountAmount =
                loyaltyDiscountAmount
                        + bulkDiscountAmount
                        + couponDiscountAmount;

        double maximumAllowedDiscount = dynamicSubtotal * 0.30;

        boolean discountCapped =
                rawDiscountAmount > maximumAllowedDiscount;

        double discountAmount = Math.min(
                rawDiscountAmount, maximumAllowedDiscount
        );

        discountAmount = Math.round(discountAmount * 100.0) / 100.0;

        order.setDiscountAmount(discountAmount);

        // -----------------------------------------------------
        // DELIVERY FEE
        // -----------------------------------------------------

        double deliveryFee =
                deliveryFeeCalculator.calculateDeliveryFee(
                        customer, restaurant
                );

        if (deliveryFee < 0) {
            return showCheckoutError(
                    model,
                    customer,
                    cart,
                    restaurant,
                    "Delivery is not available for your location."
            );
        }

        order.setDeliveryFee(deliveryFee);

        // -----------------------------------------------------
        // FINAL TOTAL
        // -----------------------------------------------------

        double finalAmount =
                dynamicSubtotal - discountAmount + deliveryFee;

        finalAmount = Math.max(0.0, finalAmount);
        finalAmount = Math.round(finalAmount * 100.0) / 100.0;

        order.setFinalAmount(finalAmount);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setOrderDateTime(orderTime);
        order.setPaymentStatus("PENDING");

        // Keep the calculated order in the session until payment confirmation.
        // The order is not saved and loyalty points are not awarded here.
        session.setAttribute(PENDING_ORDER, order);

        Map<String, Object> breakdown = new HashMap<>();

        breakdown.put("originalSubtotal", originalSubtotal);
        breakdown.put("dynamicSubtotal", dynamicSubtotal);
        breakdown.put("dynamicPricingAmount", dynamicPricingAmount);
        breakdown.put("deliveryFee", deliveryFee);
        breakdown.put("finalAmount", finalAmount);
        breakdown.put("loyaltyDiscountAmount", loyaltyDiscountAmount);
        breakdown.put("bulkDiscountAmount", bulkDiscountAmount);
        breakdown.put("couponDiscountAmount", couponDiscountAmount);
        breakdown.put("rawDiscountAmount", rawDiscountAmount);
        breakdown.put("maximumAllowedDiscount", maximumAllowedDiscount);
        breakdown.put("discountAmount", discountAmount);
        breakdown.put("discountCapped", discountCapped);
        breakdown.put(
                "couponCode",
                appliedCoupon == null ? null : appliedCoupon.getCouponCode()
        );
        breakdown.put("loyaltyTierBeforeOrder", loyaltyTierBeforeOrder);
        breakdown.put("loyaltyPointsBeforeOrder", loyaltyPointsBeforeOrder);
        breakdown.put("loyaltyDiscountPercentage", loyaltyDiscountPercentage);

        session.setAttribute(ORDER_BREAKDOWN, breakdown);

        model.addAttribute("order", order);
        model.addAttribute("customer", customer);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("breakdown", breakdown);

        return "payment";
    }

    // =========================================================
    // PAYMENT PAGE
    // =========================================================

    @GetMapping("/payment")
    public String paymentPage(HttpSession session, Model model) {

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return "redirect:/login";
        }

        Order order = (Order) session.getAttribute(PENDING_ORDER);

        if (order == null) {
            return "redirect:/checkout";
        }

        // Ensure that the pending order belongs to the logged-in customer.
        if (order.getCustomer().getCustomerId()
                != customer.getCustomerId()) {

            session.removeAttribute(PENDING_ORDER);
            session.removeAttribute(ORDER_BREAKDOWN);

            return "redirect:/checkout";
        }

        model.addAttribute("order", order);
        model.addAttribute("customer", customer);
        model.addAttribute(
                "breakdown",
                session.getAttribute(ORDER_BREAKDOWN)
        );

        return "payment";
    }

    // =========================================================
    // CONFIRM PAYMENT AND SAVE ORDER
    // =========================================================

    @PostMapping("/payment/confirm")
    public String confirmPayment(
            @RequestParam String paymentMethod,
            HttpSession session,
            Model model) {

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return "redirect:/login";
        }

        Order order = (Order) session.getAttribute(PENDING_ORDER);

        @SuppressWarnings("unchecked")
        Map<String, Object> breakdown =
                (Map<String, Object>) session.getAttribute(ORDER_BREAKDOWN);

        if (order == null || breakdown == null) {
            return "redirect:/checkout";
        }

        if (order.getCustomer().getCustomerId()
                != customer.getCustomerId()) {

            session.removeAttribute(PENDING_ORDER);
            session.removeAttribute(ORDER_BREAKDOWN);

            return "redirect:/checkout";
        }

        // Accept only the supported payment methods.
        String method = paymentMethod == null
                ? ""
                : paymentMethod.trim().toUpperCase();

        if (!List.of("UPI", "CARD", "COD").contains(method)) {
            model.addAttribute(
                    "error", "Please select a valid payment method."
            );
            model.addAttribute("order", order);
            model.addAttribute("customer", customer);
            model.addAttribute("breakdown", breakdown);

            return "payment";
        }

        // Prevent reprocessing an order that has already been saved.
        if (orderDAO.findById(order.getOrderId()) != null) {
            session.removeAttribute(PENDING_ORDER);
            session.removeAttribute(ORDER_BREAKDOWN);

            return "redirect:/orders";
        }

        order.setPaymentMethod(method);
        order.setPaymentStatus("COD".equals(method) ? "PENDING" : "PAID");

        // UPI and card payments are simulated; no real payment is processed.
        boolean saved = orderDAO.saveOrder(order);

        if (!saved) {
            model.addAttribute(
                    "error",
                    "Unable to save your order. Please try again."
            );
            model.addAttribute("order", order);
            model.addAttribute("customer", customer);
            model.addAttribute("breakdown", breakdown);

            return "payment";
        }

        // Award loyalty points only after the order has been saved.
        double pointsBeforeOrder =
                (double) breakdown.get("loyaltyPointsBeforeOrder");

        loyaltyService.processLoyaltyPoints(
                customer, order.getFinalAmount()
        );

        double loyaltyPointsEarned =
                customer.getLoyaltyPoints() - pointsBeforeOrder;

        customerDAO.updateCustomer(customer);

        // Clear the cart only after the order has been saved successfully.
        Cart cart = getCart(session);

        if (cart != null) {
            cart.clear();
            session.setAttribute("cart", cart);
        }

        // Store confirmation data for a separate GET request.
        session.setAttribute(CONFIRMED_ORDER, order);
        session.setAttribute(
                CONFIRMED_BREAKDOWN, new HashMap<>(breakdown)
        );
        session.setAttribute(
                CONFIRMED_POINTS_EARNED, loyaltyPointsEarned
        );

        // Remove the pending payment draft.
        session.removeAttribute(PENDING_ORDER);
        session.removeAttribute(ORDER_BREAKDOWN);

        // Post/Redirect/Get prevents refresh from resubmitting payment.
        return "redirect:/payment/confirmed";
    }

    // =========================================================
    // REFRESH-SAFE ORDER CONFIRMATION
    // =========================================================

    @GetMapping("/payment/confirmed")
    public String confirmedPayment(
            HttpSession session,
            Model model) {

        Customer customer = getLoggedInCustomer(session);

        if (customer == null) {
            return "redirect:/login";
        }

        Order order =
                (Order) session.getAttribute(CONFIRMED_ORDER);

        @SuppressWarnings("unchecked")
        Map<String, Object> breakdown =
                (Map<String, Object>) session.getAttribute(
                        CONFIRMED_BREAKDOWN
                );

        Object pointsObject =
                session.getAttribute(CONFIRMED_POINTS_EARNED);

        // No confirmation exists in this session; do not create an order.
        if (order == null
                || breakdown == null
                || pointsObject == null) {
            return "redirect:/orders";
        }

        // Customers may view only their own order confirmation.
        if (order.getCustomer() == null
                || order.getCustomer().getCustomerId()
                != customer.getCustomerId()) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("customer", customer);
        model.addAttribute("restaurant", order.getRestaurant());

        for (Map.Entry<String, Object> entry : breakdown.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }

        model.addAttribute("loyaltyPointsEarned", pointsObject);

        return "order-confirmation";
    }
    
@GetMapping("/orders/{orderId}")
public String viewOrderDetails(
        @org.springframework.web.bind.annotation.PathVariable int orderId,
        HttpSession session,
        Model model) {

    Customer customer = getLoggedInCustomer(session);

    if (customer == null) {
        return "redirect:/login";
    }

    // Load the saved order from SQLite.
    Order order = orderDAO.findById(orderId);

    if (order == null) {
        return "redirect:/orders";
    }

    // Customers can view only their own orders.
    if (order.getCustomer() == null
            || order.getCustomer().getCustomerId()
            != customer.getCustomerId()) {
        return "redirect:/orders";
    }

    model.addAttribute("order", order);
    model.addAttribute("customer", customer);
    model.addAttribute("restaurant", order.getRestaurant());

    return "order-details";
}

}
