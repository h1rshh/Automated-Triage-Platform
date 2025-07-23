package com.example.payments.processor;

import com.example.payments.model.PaymentRequest;
import com.example.payments.model.PaymentResult;

// This is a fake payment processing class.
public class PaymentProcessor {

    /**
     * Processes a payment transaction.
     * This is a critical function that can fail under high load.
     * It interacts with external APIs and the 'transactions' database table.
     */
    public PaymentResult process_payment(PaymentRequest request) {
        if (request == null) {
            // A common source of Null Pointer Exceptions.
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        // ... complex payment logic ...
        return new PaymentResult("SUCCESS");
    }
}
