package com.filadelfia.store.filadelfiastore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Test controller for Stripe payment integration
 */
@Controller
@RequestMapping("/test")
public class StripeTestController {

    /**
     * Show Stripe payment test page
     */
    @GetMapping("/stripe-payment")
    public String stripePaymentTest() {
        return "stripe-payment";
    }
}
