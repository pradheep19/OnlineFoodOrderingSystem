package com.foodordering.system.controller;

import com.foodordering.system.dao.CustomerDAO;
import com.foodordering.system.model.Customer;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final CustomerDAO customerDAO;

    public LoginController(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    // =========================================================
    // LOGIN PAGE
    // =========================================================

    @GetMapping("/login")
    public String loginPage(
            HttpSession session,
            Model model) {

        // If already logged in, go directly to restaurants
        Customer loggedInCustomer =
                (Customer) session.getAttribute("loggedInCustomer");

        if (loggedInCustomer != null) {
            return "redirect:/restaurants";
        }

        model.addAttribute(
                "customers",
                customerDAO.findAll()
        );

        return "login";
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public String login(
            @RequestParam("customerId") int customerId,
            HttpSession session,
            Model model) {

        Customer customer =
                customerDAO.findById(customerId);

        // -----------------------------------------------------
        // INVALID CUSTOMER
        // -----------------------------------------------------

        if (customer == null) {

            model.addAttribute(
                    "error",
                    "Customer not found."
            );

            model.addAttribute(
                    "customers",
                    customerDAO.findAll()
            );

            return "login";
        }


        // -----------------------------------------------------
        // STORE CUSTOMER IN SESSION
        // -----------------------------------------------------

        session.setAttribute(
                "loggedInCustomer",
                customer
        );


        // -----------------------------------------------------
        // CREATE A NEW CART FOR THIS CUSTOMER
        // -----------------------------------------------------

        session.removeAttribute("cart");


        // -----------------------------------------------------
        // GO TO RESTAURANTS
        // -----------------------------------------------------

        return "redirect:/restaurants";
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        // Remove logged-in customer
        session.removeAttribute("loggedInCustomer");

        // Remove current customer's cart
        session.removeAttribute("cart");

        return "redirect:/login";
    }
}