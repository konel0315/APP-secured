package com.example.payment;

import lombok.Data;

@Data
public class CardRequest {
    private String cardNumber;
    private String cvc;
    private String expiryDate;
}
